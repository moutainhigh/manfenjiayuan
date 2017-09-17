package com.jabra.bean;

import com.tencent.mm.sdk.ext.MMOpenApiCaller;

import java.io.File;

public class PlayableItem {
    public static final int TYPE_AMR = 1;
    public static final int TYPE_PCM = 2;
    public static final int TYPE_RES = 4;
    public static final int TYPE_TTS = 3;
    private File file;
    private int resId;
    private String text;
    private int type;

    public PlayableItem(int paramInt) {
        this.type = 4;
        this.resId = paramInt;
    }

    public PlayableItem(File paramFile) {
        if (MMOpenApiCaller.isAmr(paramFile.getAbsolutePath())) {
        }
        for (this.type = 1; ; this.type = 2) {
            this.file = paramFile;
            return;
        }
    }

    public PlayableItem(String paramString) {
        this.type = 3;
        this.text = paramString;
    }

    public boolean equals(Object paramObject) {
        if (!(paramObject instanceof PlayableItem)) {
            return false;
        }
        PlayableItem localPlayableItem = (PlayableItem) paramObject;
        if ((localPlayableItem.resId != this.resId)) {
            if ((localPlayableItem.type != this.type)) {
                switch (getType()) {
                    case 1:
                    case 2:
                        return localPlayableItem.file.getAbsolutePath().equals(this.file.getAbsolutePath());
                    default:
                        return false;
                }
            }
        }

        return localPlayableItem.text.equals(this.text);
    }

    public File getFile() {
        return this.file;
    }

    public int getResId() {
        return this.resId;
    }

    public String getText() {
        return this.text;
    }

    public int getType() {
        return this.type;
    }

    public void setFile(File paramFile) {
        this.file = paramFile;
    }

    public void setResId(int paramInt) {
        this.resId = paramInt;
    }

    public void setText(String paramString) {
        this.text = paramString;
    }

    public void setType(int paramInt) {
        this.type = paramInt;
    }

    public String toString() {
        return "PlayableItem [type=" + this.type + ", file=" + this.file + ", text=" + this.text + ", resId=" + this.resId + "]";
    }
}
