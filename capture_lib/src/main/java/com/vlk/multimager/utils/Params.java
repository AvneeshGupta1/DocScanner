package com.vlk.multimager.utils;

import java.io.Serializable;

/**
 * Created by vansikrishna on 6/13/2016.
 */
public class Params implements Serializable {

    private int pickerLimit;
    private int captureLimit;
    private int toolbarColor;
    private int lightColor;
    private int actionButtonColor;
    private int buttonTextColor;
    private int columnCount;
    private int thumbnailWidthInDp;

    public Params() {
    }

    public int getPickerLimit() {
        return pickerLimit;
    }

    public void setPickerLimit(int pickerLimit) {
        this.pickerLimit = pickerLimit;
    }

    public int getCaptureLimit() {
        return captureLimit;
    }

    public void setCaptureLimit(int captureLimit) {
        this.captureLimit = captureLimit;
    }

    public int getToolbarColor() {
        return toolbarColor;
    }

    public void setToolbarColor(int toolbarColor) {
        this.toolbarColor = toolbarColor;
    }

    public int getActionButtonColor() {
        return actionButtonColor;
    }

    public void setActionButtonColor(int actionButtonColor) {
        this.actionButtonColor = actionButtonColor;
    }

    public int getButtonTextColor() {
        return buttonTextColor;
    }

    public void setButtonTextColor(int buttonTextColor) {
        this.buttonTextColor = buttonTextColor;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public int getThumbnailWidthInDp() {
        return thumbnailWidthInDp;
    }

    public void setThumbnailWidthInDp(int thumbnailWidthInDp) {
        this.thumbnailWidthInDp = thumbnailWidthInDp;
    }

    public int getLightColor() {
        if (lightColor != 0) {
            return lightColor;
        } else {
            if (toolbarColor != 0)
                lightColor = Utils.getLightColor(toolbarColor);
            return lightColor;
        }
    }
}
