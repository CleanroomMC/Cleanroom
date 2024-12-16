package org.lwjglx.input;

public interface Controller {

    public abstract int getAxisCount();

    public abstract java.lang.String getAxisName(int arg0);

    public abstract float getAxisValue(int arg0);

    public abstract int getButtonCount();

    public abstract java.lang.String getButtonName(int arg0);

    public abstract float getDeadZone(int arg0);

    public abstract int getIndex();

    public abstract java.lang.String getName();

    public abstract float getPovX();

    public abstract float getPovY();

    public abstract float getRXAxisDeadZone();

    public abstract float getRXAxisValue();

    public abstract float getRYAxisDeadZone();

    public abstract float getRYAxisValue();

    public abstract float getRZAxisDeadZone();

    public abstract float getRZAxisValue();

    public abstract int getRumblerCount();

    public abstract java.lang.String getRumblerName(int arg0);

    public abstract float getXAxisDeadZone();

    public abstract float getXAxisValue();

    public abstract float getYAxisDeadZone();

    public abstract float getYAxisValue();

    public abstract float getZAxisDeadZone();

    public abstract float getZAxisValue();

    public abstract boolean isButtonPressed(int arg0);

    public abstract void poll();

    public abstract void setDeadZone(int arg0, float arg1);

    public abstract void setRXAxisDeadZone(float arg0);

    public abstract void setRYAxisDeadZone(float arg0);

    public abstract void setRZAxisDeadZone(float arg0);

    public abstract void setRumblerStrength(int arg0, float arg1);

    public abstract void setXAxisDeadZone(float arg0);

    public abstract void setYAxisDeadZone(float arg0);

    public abstract void setZAxisDeadZone(float arg0);
}