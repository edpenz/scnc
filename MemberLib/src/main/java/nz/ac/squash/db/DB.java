package nz.ac.squash.db;

import nz.ac.squash.util.Utility;
import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

public class DB {
    private static Logger sLogger = Logger.getLogger(DB.class);

    private static final SessionFactory sSessionFactory;

    private static final BlockingQueue<DB.Transaction<?>> sPendingTransactions = new ArrayBlockingQueue<>(
            128);

    private static Transaction<?> sRunning = null;
    private static final Thread sExecutor = new Thread(() -> {
        while (!Thread.interrupted()) {
            try {
                sRunning = sPendingTransactions.take();
            } catch (InterruptedException e) {
                break;
            }

            try {
                long start = System.currentTimeMillis();

                sRunning.begin();
                sRunning.run();
                sRunning.end();

                long end = System.currentTimeMillis();
                sLogger.trace((end - start) + "ms to do " +
                        sRunning.dQueuedFrom.toString());
            } catch (Exception ex) {
                sLogger.error("Transaction failed", ex);
            }

            sRunning = null;
        }
    });

    static {
        final Configuration configuration = new Configuration().configure();

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties()).build();
        sSessionFactory = configuration.buildSessionFactory(serviceRegistry);

        sExecutor.start();
    }

    public static <T> T executeTransaction(Transaction<T> t) {
        queueTransaction(t);
        t.waitUntilDone();
        return t.getResult();
    }

    public static <T> T executeTransaction(Supplier<T> s) {
        Transaction<T> t = queueTransaction(s);
        t.waitUntilDone();
        return t.getResult();
    }

    public static void executeTransaction(Runnable r) {
        Transaction<Void> t = queueTransaction(r);
        t.waitUntilDone();
    }

    public static <T> Transaction<T> queueTransaction(Transaction<T> t) {
        if (Thread.currentThread() == sExecutor) {
            t.attach(sRunning);
            t.run();
            t.detach();
        } else {
            t.dQueuedFrom = Utility.getOuterTrace(DB.class);
            try {
                sPendingTransactions.put(t);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }

        return t;
    }

    public static <T> Transaction<T> queueTransaction(Supplier<T> s) {
        Transaction<T> t = new Transaction<T>() {
            @Override
            public void run() {
                setResult(s.get());
            }
        };

        return queueTransaction(t);
    }

    public static Transaction<Void> queueTransaction(Runnable r) {
        Transaction<Void> t = new Transaction<Void>() {
            @Override
            public void run() {
                r.run();
            }
        };

        return queueTransaction(t);
    }

    public static List<?> query(String hqlQuery, Object... params) {
        final Query query = sRunning.mSession.createQuery(hqlQuery);

        for (int i = 0; i < params.length; i++) {
            query.setParameter(String.valueOf(i), params[i]);
        }

        return query.list();
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> typedQuery(Class<T> clazz, String hqlQuery, Object... params) {
        final Query query = sRunning.mSession.createQuery(hqlQuery);

        for (int i = 0; i < params.length; i++) {
            query.setParameter(String.valueOf(i), params[i]);
        }

        final List<?> results = query.list();

        final List<T> filteredResults = new ArrayList<>();
        for (Object row : results) {
            if (clazz.isInstance(row)) {
                filteredResults.add((T) row);
            } else if (row instanceof Object[]) {
                for (Object cell : (Object[]) row) {
                    if (clazz.isInstance(cell)) {
                        filteredResults.add((T) cell);
                        break;
                    }
                }
            }
        }
        return filteredResults;
    }

    // Shorthand for simple queries against a single table.
    @SuppressWarnings("unchecked")
    public static <T> List<T> query(Class<T> type, String hqlQuery, Object... params) {
        return (List<T>) query("from " + type.getName() + " " + hqlQuery, params);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> listAll(Class<T> clazz) {
        return sRunning.mSession.createQuery("from " + clazz.getName()).list();
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz, long id) {
        return (T) sRunning.mSession.get(clazz, id);
    }

    public static abstract class Transaction<R> implements Runnable {
        private Session mSession;
        private Transaction<?> mParent = null;

        private boolean mIsDirty = false;

        private R mResult = null;
        private boolean mHasFinished = false;
        private List<Runnable> mDoAfter = null;

        private StackTraceElement dQueuedFrom;

        private void attach(Transaction<?> parent) {
            mParent = parent;
            mSession = mParent.mSession;
        }

        private void detach() {
            mParent = null;
            mSession = null;

            synchronized (this) {
                mHasFinished = true;
                notifyAll();
            }
        }

        private void begin() {
            sSessionFactory.getCache().evictAllRegions();

            mSession = sSessionFactory.openSession();
            mSession.beginTransaction();
        }

        private void end() {
            try {
                if (mIsDirty) {
                    mSession.getTransaction().commit();
                }
            } finally {
                mSession.close();

                synchronized (this) {
                    mHasFinished = true;
                    notifyAll();

                    if (mDoAfter != null) {
                        for (Runnable toDo : mDoAfter) {
                            toDo.run();
                        }
                    }
                }
            }
        }

        private void dirty() {
            if (mParent != null) {
                mParent.dirty();
            } else {
                mIsDirty = true;
            }
        }

        public synchronized boolean waitUntilDone() {
            while (!mHasFinished) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    return false;
                }
            }

            return true;
        }

        public synchronized void doWhenDone(Runnable task) {
            if (mHasFinished) {
                task.run();
            } else {
                if (mDoAfter == null) {
                    mDoAfter = new ArrayList<>();
                }
                mDoAfter.add(task);
            }
        }

        public R getResult() {
            return mResult;
        }

        protected void setResult(R result) {
            mResult = result;
        }

        protected List<?> query(String hqlQuery, Object... params) {
            return DB.query(hqlQuery, params);
        }

        protected <T> List<T> typedQuery(Class<T> clazz, String hqlQuery, Object... params) {
            return DB.typedQuery(clazz, hqlQuery, params);
        }

        protected <T> List<T> query(Class<T> type, String hqlQuery, Object... params) {
            return DB.query(type, hqlQuery, params);
        }

        protected <T> List<T> listAll(Class<T> clazz) {
            return DB.listAll(clazz);
        }

        protected <T> T get(Class<T> clazz, long id) {
            return DB.get(clazz, id);
        }

        protected void attach(Object object) {
            final LockOptions opts = new LockOptions(LockMode.NONE);
            mSession.buildLockRequest(opts).lock(object);
        }

        protected void update(Object object) {
            mSession.saveOrUpdate(object);
            dirty();
        }

        protected void delete(Object object) {
            mSession.delete(object);
            dirty();
        }
    }
}
