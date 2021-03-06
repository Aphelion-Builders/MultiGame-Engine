/*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
 *
 * Licensed under the Academic Free License v. 3.0.
 * http://www.opensource.org/licenses/afl-3.0.php
 */

/**
 * 
 * 
 * @author awaterma@ecosur.mx
 */

package mx.ecosur.multigame.grid.util;

import mx.ecosur.multigame.grid.enums.Direction;
import mx.ecosur.multigame.grid.entity.GridCell;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class AnnotatedCell {
        
    private Direction direction;

    private GridCell cell;

    public AnnotatedCell (GridCell cell) {
            this.cell = cell;
            this.direction = Direction.UNKNOWN;
    }

    public AnnotatedCell (GridCell cell, Direction direction) {
            this.cell = cell;
            this.direction = direction;
    }

    public Direction getDirection() {
            return direction;
    }

    public void setDirection(Direction direction) {
            this.direction = direction;
    }

    public GridCell getCell() {
            return cell;
    }

    public String toString() {
            return direction.toString() + ", " + cell.toString();
    }

    public boolean equals(Object obj) {
        if (obj instanceof AnnotatedCell) {
            AnnotatedCell comparison = (AnnotatedCell) obj;
            return (comparison.cell.equals(this.cell) &&
                            comparison.direction.equals(this.direction));
        } else
            return super.equals(obj);
    }

    public int hashCode() {
        return new HashCodeBuilder().append(cell).append(direction).hashCode();
    }
}
