package nz.ac.squash.widget.generic;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Insets;

public class VerticalGridLayout extends GridLayout {
    private static final long serialVersionUID = 1L;

    public VerticalGridLayout(int rows, int cols, int hgap, int vgap) {
        super(rows, cols, hgap, vgap);
    }

    /**
     * Lays out the specified container using this layout.
     * <p>
     * This method reshapes the components in the specified target container in
     * order to satisfy the constraints of the <code>GridLayout</code> object.
     * <p>
     * The grid layout manager determines the size of individual components by
     * dividing the free space in the container into equal-sized portions
     * according to the number of rows and columns in the layout. The
     * container's free space equals the container's size minus any insets and
     * any specified horizontal or vertical gap. All components in a grid layout
     * are given the same size.
     * 
     * @param parent
     *            the container in which to do the layout
     * @see java.awt.Container
     * @see java.awt.Container#doLayout
     */
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Insets insets = parent.getInsets();
            int ncomponents = parent.getComponentCount();
            int nrows = getRows();
            int ncols = getColumns();
            int hgap = getHgap();
            int vgap = getVgap();

            if (ncomponents == 0) {
                return;
            }
            if (nrows > 0) {
                ncols = (ncomponents + nrows - 1) / nrows;
            } else {
                nrows = (ncomponents + ncols - 1) / ncols;
            }
            // 4370316. To position components in the center we should:
            // 1. get an amount of extra space within Container
            // 2. incorporate half of that value to the left/top position
            // Note that we use truncating division for widthOnComponent
            // The reminder goes to extraWidthAvailable
            int totalGapsWidth = (ncols - 1) * hgap;
            int widthWOInsets = parent.getWidth() -
                                (insets.left + insets.right);
            int widthForComponents = widthWOInsets - totalGapsWidth;

            int totalGapsHeight = (nrows - 1) * vgap;
            int heightWOInsets = parent.getHeight() -
                                 (insets.top + insets.bottom);
            int heightForComponents = heightWOInsets - totalGapsHeight;

            for (int row = 0; row < nrows; row++) {
                for (int col = 0; col < ncols; col++) {
                    int i = col * nrows + row;

                    if (i < ncomponents) {
                        int l = insets.left + col * hgap +
                                (col * widthForComponents / ncols);
                        int r = insets.left + col * hgap +
                                ((col + 1) * widthForComponents / ncols);

                        int t = insets.top + row * vgap +
                                (row * heightForComponents / nrows);
                        int b = insets.top + row * vgap +
                                ((row + 1) * heightForComponents / nrows);

                        parent.getComponent(i).setBounds(l, t, r - l, b - t);
                    }
                }
            }
        }
    }
}