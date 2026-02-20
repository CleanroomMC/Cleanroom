package com.cleanroommc.catalogue.client.screen.layout;

import com.cleanroommc.catalogue.Utils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

@SideOnly(Side.CLIENT)
public class GridLayout extends AbstractLayout {
    private final List<LayoutElement> children = new ArrayList<>();
    private final List<GridLayout.CellInhabitant> cellInhabitants = new ArrayList<>();
    private final LayoutSettings defaultCellSettings = LayoutSettings.defaults();
    private int rowSpacing = 0;
    private int columnSpacing = 0;

    public GridLayout() {
        this(0, 0);
    }

    public GridLayout(int x, int y) {
        super(x, y, 0, 0);
    }

    @Override
    public void arrangeElements() {
        super.arrangeElements();
        int i = 0;
        int j = 0;

        for (GridLayout.CellInhabitant gridlayout$cellinhabitant : this.cellInhabitants) {
            i = Math.max(gridlayout$cellinhabitant.getLastOccupiedRow(), i);
            j = Math.max(gridlayout$cellinhabitant.getLastOccupiedColumn(), j);
        }

        int[] aint = new int[j + 1];
        int[] aint1 = new int[i + 1];

        for (GridLayout.CellInhabitant gridlayout$cellinhabitant1 : this.cellInhabitants) {
            int k = gridlayout$cellinhabitant1.getHeight() - (gridlayout$cellinhabitant1.occupiedRows - 1) * this.rowSpacing;
            Divisor divisor = new Divisor(k, gridlayout$cellinhabitant1.occupiedRows);

            for (int l = gridlayout$cellinhabitant1.row; l <= gridlayout$cellinhabitant1.getLastOccupiedRow(); l++) {
                aint1[l] = Math.max(aint1[l], divisor.nextInt());
            }

            int l1 = gridlayout$cellinhabitant1.getWidth() - (gridlayout$cellinhabitant1.occupiedColumns - 1) * this.columnSpacing;
            Divisor divisor1 = new Divisor(l1, gridlayout$cellinhabitant1.occupiedColumns);

            for (int i1 = gridlayout$cellinhabitant1.column; i1 <= gridlayout$cellinhabitant1.getLastOccupiedColumn(); i1++) {
                aint[i1] = Math.max(aint[i1], divisor1.nextInt());
            }
        }

        int[] aint2 = new int[j + 1];
        int[] aint3 = new int[i + 1];
        aint2[0] = 0;

        for (int j1 = 1; j1 <= j; j1++) {
            aint2[j1] = aint2[j1 - 1] + aint[j1 - 1] + this.columnSpacing;
        }

        aint3[0] = 0;

        for (int k1 = 1; k1 <= i; k1++) {
            aint3[k1] = aint3[k1 - 1] + aint1[k1 - 1] + this.rowSpacing;
        }

        for (GridLayout.CellInhabitant gridlayout$cellinhabitant2 : this.cellInhabitants) {
            int i2 = 0;

            for (int j2 = gridlayout$cellinhabitant2.column; j2 <= gridlayout$cellinhabitant2.getLastOccupiedColumn(); j2++) {
                i2 += aint[j2];
            }

            i2 += this.columnSpacing * (gridlayout$cellinhabitant2.occupiedColumns - 1);
            gridlayout$cellinhabitant2.setX(this.getX() + aint2[gridlayout$cellinhabitant2.column], i2);
            int k2 = 0;

            for (int l2 = gridlayout$cellinhabitant2.row; l2 <= gridlayout$cellinhabitant2.getLastOccupiedRow(); l2++) {
                k2 += aint1[l2];
            }

            k2 += this.rowSpacing * (gridlayout$cellinhabitant2.occupiedRows - 1);
            gridlayout$cellinhabitant2.setY(this.getY() + aint3[gridlayout$cellinhabitant2.row], k2);
        }

        this.width = aint2[j] + aint[j];
        this.height = aint3[i] + aint1[i];
    }

    public <T extends LayoutElement> T addChild(T child, int row, int column) {
        return this.addChild(child, row, column, this.newCellSettings());
    }

    public <T extends LayoutElement> T addChild(T child, int row, int column, LayoutSettings layoutSettings) {
        return this.addChild(child, row, column, 1, 1, layoutSettings);
    }

    public <T extends LayoutElement> T addChild(T child, int row, int column, UnaryOperator<LayoutSettings> layoutSettingsFactory) {
        return this.addChild(child, row, column, 1, 1, layoutSettingsFactory.apply(this.newCellSettings()));
    }

    public <T extends LayoutElement> T addChild(T child, int row, int column, int occupiedRows, int occupiedColumns) {
        return this.addChild(child, row, column, occupiedRows, occupiedColumns, this.newCellSettings());
    }

    public <T extends LayoutElement> T addChild(T child, int row, int column, int occupiedRows, int occupiedColumns, LayoutSettings layoutSettings) {
        if (occupiedRows < 1) {
            throw new IllegalArgumentException("Occupied rows must be at least 1");
        } else if (occupiedColumns < 1) {
            throw new IllegalArgumentException("Occupied columns must be at least 1");
        } else {
            this.cellInhabitants.add(new GridLayout.CellInhabitant(child, row, column, occupiedRows, occupiedColumns, layoutSettings));
            this.children.add(child);
            return child;
        }
    }

    public <T extends LayoutElement> T addChild(T child, int row, int column, int occupiedRows, int occupiedColumns, UnaryOperator<LayoutSettings> layoutSettingsFactory) {
        return this.addChild(child, row, column, occupiedRows, occupiedColumns, layoutSettingsFactory.apply(this.newCellSettings()));
    }

    public GridLayout columnSpacing(int columnSpacing) {
        this.columnSpacing = columnSpacing;
        return this;
    }

    public GridLayout rowSpacing(int rowSpacing) {
        this.rowSpacing = rowSpacing;
        return this;
    }

    public GridLayout spacing(int spacing) {
        return this.columnSpacing(spacing).rowSpacing(spacing);
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> visitor) {
        this.children.forEach(visitor);
    }

    public LayoutSettings newCellSettings() {
        return this.defaultCellSettings.copy();
    }

    public LayoutSettings defaultCellSetting() {
        return this.defaultCellSettings;
    }

    public GridLayout.RowHelper createRowHelper(int columns) {
        return new GridLayout.RowHelper(columns);
    }

    @SideOnly(Side.CLIENT)
    static class CellInhabitant extends AbstractLayout.AbstractChildWrapper {
        final int row;
        final int column;
        final int occupiedRows;
        final int occupiedColumns;

        CellInhabitant(LayoutElement child, int row, int column, int occupiedRows, int occupiedColumns, LayoutSettings layoutSettings) {
            super(child, layoutSettings.getExposed());
            this.row = row;
            this.column = column;
            this.occupiedRows = occupiedRows;
            this.occupiedColumns = occupiedColumns;
        }

        public int getLastOccupiedRow() {
            return this.row + this.occupiedRows - 1;
        }

        public int getLastOccupiedColumn() {
            return this.column + this.occupiedColumns - 1;
        }
    }

    @SideOnly(Side.CLIENT)
    public final class RowHelper {
        private final int columns;
        private int index;

        RowHelper(int columns) {
            this.columns = columns;
        }

        public <T extends LayoutElement> T addChild(T child) {
            return this.addChild(child, 1);
        }

        public <T extends LayoutElement> T addChild(T child, int occupiedColumns) {
            return this.addChild(child, occupiedColumns, this.defaultCellSetting());
        }

        public <T extends LayoutElement> T addChild(T child, LayoutSettings layoutSettings) {
            return this.addChild(child, 1, layoutSettings);
        }

        public <T extends LayoutElement> T addChild(T child, int occupiedColumns, LayoutSettings layoutSettings) {
            int i = this.index / this.columns;
            int j = this.index % this.columns;
            if (j + occupiedColumns > this.columns) {
                i++;
                j = 0;
                this.index = Utils.roundToward(this.index, this.columns);
            }

            this.index += occupiedColumns;
            return GridLayout.this.addChild(child, i, j, 1, occupiedColumns, layoutSettings);
        }

        public GridLayout getGrid() {
            return GridLayout.this;
        }

        public LayoutSettings newCellSettings() {
            return GridLayout.this.newCellSettings();
        }

        public LayoutSettings defaultCellSetting() {
            return GridLayout.this.defaultCellSetting();
        }
    }
}
