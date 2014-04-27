package nz.ac.squash.db;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import nz.ac.squash.util.Utility;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class DB {
	private static Logger sLogger = Logger.getLogger(DB.class);

	private static final SessionFactory sSessionFactory;

	private static final BlockingQueue<DB.Transaction> sPendingTransactions = new ArrayBlockingQueue<>(
			128);

	private static Transaction sRunning = null;
	private static final Thread sExecutor = new Thread(new Runnable() {
		@Override
		public void run() {
			while (!Thread.interrupted()) {
				try {
					sRunning = sPendingTransactions.take();
				} catch (InterruptedException e) {
					break;
				}

				try {
					sRunning.begin();
					sRunning.run();
					sRunning.end();
				} catch (Exception ex) {
					sLogger.error("Transaction failed", ex);
					Utility.restart();
				}

				sRunning = null;
			}
		}
	});

	static {
		final Configuration configuration = new Configuration().configure();

		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties()).build();
		sSessionFactory = configuration.buildSessionFactory(serviceRegistry);

		sExecutor.start();
	}

	public static void executeTransaction(Transaction t) {
		queueTransaction(t);
		t.waitUntilDone();
	}

	public static Transaction queueTransaction(Transaction t) {
		if (Thread.currentThread() == sExecutor) {
			t.attach(sRunning);
			t.run();
			t.detach();
		} else {
			sLogger.debug("Transaction queued from "
					+ Utility.getOuterTrace(DB.class).toString());

			try {
				sPendingTransactions.put(t);
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}

		return t;
	}

	public static abstract class Transaction implements Runnable {
		private Session mSession;
		private Transaction mParent = null;

		private boolean mIsDirty = false;

		private boolean mHasFinished = false;
		private List<Runnable> mDoAfter = null;

		private void attach(Transaction parent) {
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
					mDoAfter = new ArrayList<Runnable>();
				}
				mDoAfter.add(task);
			}
		}

		protected List<?> query(String hqlQuery, Object... params) {
			final Query query = mSession.createQuery(hqlQuery);

			for (int i = 0; i < params.length; i++) {
				query.setParameter(String.valueOf(i), params[i]);
			}

			return query.list();
		}

		@SuppressWarnings("unchecked")
		protected <T> List<T> typedQuery(Class<T> clazz, String hqlQuery,
				Object... params) {
			final Query query = mSession.createQuery(hqlQuery);

			for (int i = 0; i < params.length; i++) {
				query.setParameter(String.valueOf(i), params[i]);
			}

			final List<?> results = query.list();

			final List<T> filteredResults = new ArrayList<T>();
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
		protected <T> List<T> query(Class<T> type, String hqlQuery,
				Object... params) {
			return (List<T>) query("from " + type.getName() + " " + hqlQuery,
					params);
		}

		@SuppressWarnings("unchecked")
		protected <T> List<T> listAll(Class<T> clazz) {
			return mSession.createQuery("from " + clazz.getName()).list();
		}

		@SuppressWarnings("unchecked")
		protected <T> T get(Class<T> clazz, long id) {
			return (T) mSession.get(clazz, id);
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
