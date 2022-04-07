package com.github.burgerguy.recordable.client;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.core.Direction;

public class QuaternionTest {
    private static final Vector3f NORTH = new Vector3f(0, 0, -1);
    private static final Vector3f EAST = new Vector3f(1, 0, 0);
    private static final Vector3f SOUTH = new Vector3f(0, 0, 1);
    private static final Vector3f WEST = new Vector3f(-1, 0, 0);
    private static final Vector3f UP = new Vector3f(0, 1, 0);
    private static final Vector3f DOWN = new Vector3f(0, -1, 0);

    private static final Vector3f FRONT = new Vector3f(0, 0, -1);
    private static final Vector3f RIGHT = new Vector3f(1, 0, 0);
    private static final Vector3f BACK = new Vector3f(0, 0, 1);
    private static final Vector3f LEFT = new Vector3f(-1, 0, 0);
    private static final Vector3f ABOVE = new Vector3f(0, 1, 0);
    private static final Vector3f BELOW = new Vector3f(0, -1, 0);

    public static void main(String[] args) {
        assertRotate(Direction.NORTH, NORTH, FRONT);
        assertRotate(Direction.NORTH, EAST, RIGHT);
        assertRotate(Direction.NORTH, SOUTH, BACK);
        assertRotate(Direction.NORTH, WEST, LEFT);
        assertRotate(Direction.NORTH, UP, ABOVE);
        assertRotate(Direction.NORTH, DOWN, BELOW);

        assertRotate(Direction.EAST, NORTH, LEFT);
        assertRotate(Direction.EAST, EAST, FRONT);
        assertRotate(Direction.EAST, SOUTH, RIGHT);
        assertRotate(Direction.EAST, WEST, BACK);
        assertRotate(Direction.EAST, UP, ABOVE);
        assertRotate(Direction.EAST, DOWN, BELOW);

        assertRotate(Direction.SOUTH, NORTH, BACK);
        assertRotate(Direction.SOUTH, EAST, LEFT);
        assertRotate(Direction.SOUTH, SOUTH, FRONT);
        assertRotate(Direction.SOUTH, WEST, RIGHT);
        assertRotate(Direction.SOUTH, UP, ABOVE);
        assertRotate(Direction.SOUTH, DOWN, BELOW);

        assertRotate(Direction.WEST, NORTH, RIGHT);
        assertRotate(Direction.WEST, EAST, BACK);
        assertRotate(Direction.WEST, SOUTH, LEFT);
        assertRotate(Direction.WEST, WEST, FRONT);
        assertRotate(Direction.WEST, UP, ABOVE);
        assertRotate(Direction.WEST, DOWN, BELOW);
    }

    private static void assertRotate(Direction blockFacing, Vector3f soundSource, Vector3f expected) {
        Quaternion quaternion = switch(blockFacing) {
            case NORTH -> Quaternion.ONE;
            case EAST -> Vector3f.YP.rotationDegrees(90.0F);
            case SOUTH -> Vector3f.YP.rotationDegrees(180.0F);
            case WEST -> Vector3f.YP.rotationDegrees(-90.0F);
            default -> throw new IllegalStateException("Unexpected rotation value: " + blockFacing);
        };
//        if (!blockFacing.equals(Direction.NORTH)) {
//            quaternion.mul(Vector3f.XP.rotationDegrees(-90.0F));
//            quaternion.mul(blockFacing.getClockWise().getClockWise().getRotation()); // turn 180 degrees
////            quaternion.mul(Direction.SOUTH.getRotation());
//        }
        Vector3f rotated = soundSource.copy();
        rotated.transform(quaternion);
        if (!equalsish(expected, rotated)) {
            throw new RuntimeException("Expected " + expected + " for direction " + blockFacing + ", but got " + rotated);
        }
    }

    private static boolean equalsish(Vector3f a, Vector3f b) {
        return Math.abs(a.x() - b.x()) < 0.001 && Math.abs(a.y() - b.y()) < 0.001 && Math.abs(a.z() - b.z()) < 0.001;
    }

    public static Quaternion product(Quaternion a, Quaternion b) {
        Quaternion result = a.copy();
        result.mul(b);
        return result;
    }
}
