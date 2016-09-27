package com.hang.exoplayer.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * 媒体D_Media
 *
 * @author suchengdong
 */
public class MediaBean implements Serializable, Parcelable {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(mediaType);
        parcel.writeString(mediaFM);
        parcel.writeString(mediaTag);
        parcel.writeString(currentProgramId);
        parcel.writeString(show);
        parcel.writeString(currentProgram);
        parcel.writeString(notes);
        parcel.writeString(del);
        parcel.writeString(mediaUrl);
        parcel.writeString(programTimePeriod);
        parcel.writeString(icon);
        parcel.writeString(name);
        parcel.writeString(numberOfListener);
        parcel.writeString(mediaId);
        parcel.writeString(mediaTimestamp);
        parcel.writeString(mediaDynamicUrl);
    }

    public static final Creator<MediaBean> CREATOR = new Creator<MediaBean>() {
        @Override
        public MediaBean createFromParcel(Parcel parcel) {
            return new MediaBean(parcel);
        }

        @Override
        public MediaBean[] newArray(int i) {
            return new MediaBean[i];
        }
    };

    private MediaBean(Parcel parcel) {
        id = parcel.readString();
        mediaType = parcel.readString();
        mediaFM = parcel.readString();
        mediaTag = parcel.readString();
        currentProgramId = parcel.readString();
        show = parcel.readString();
        currentProgram = parcel.readString();
        notes = parcel.readString();
        del = parcel.readString();
        mediaUrl = parcel.readString();
        programTimePeriod = parcel.readString();
        icon = parcel.readString();
        name = parcel.readString();
        numberOfListener = parcel.readString();
        mediaId = parcel.readString();
        mediaTimestamp = parcel.readString();
        mediaDynamicUrl = parcel.readString();

    }

    private static final long serialVersionUID = 2L;

    @SerializedName(MediaField.rowKey)
    private String id;
    @SerializedName(MediaField.type)
    private String mediaType;
    @SerializedName(MediaField.FM)
    private String mediaFM;
    @SerializedName(MediaField.tag)
    private String mediaTag;
    @SerializedName(MediaField.currentProgramId)
    private String currentProgramId;
    @SerializedName(MediaField.show)
    private String show;
    @SerializedName(MediaField.currentProgram)
    private String currentProgram;
    @SerializedName(MediaField.notes)
    private String notes;
    @SerializedName(MediaField.del)
    private String del;
    @SerializedName(MediaField.url)
    private String mediaUrl;
    @SerializedName(MediaField.peroid)
    private String programTimePeriod;
    @SerializedName(MediaField.icon)
    private String icon;
    @SerializedName(MediaField.name)
    private String name;
    @SerializedName(MediaField.listener)
    private String numberOfListener;
    @SerializedName(MediaField.mediaId)
    private String mediaId;
    @SerializedName(MediaField.mediaTimestamp)
    private String mediaTimestamp;
    @SerializedName(MediaField.mediaDynamicUrl)
    private String mediaDynamicUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaTag() {
        return mediaTag;
    }

    public void setMediaTag(String mediaTag) {
        this.mediaTag = mediaTag;
    }

    public String getCurrentProgramId() {
        return currentProgramId;
    }

    public void setCurrentProgramId(String currentProgramId) {
        this.currentProgramId = currentProgramId;
    }

    public String getShow() {
        return show;
    }

    public void setShow(String show) {
        this.show = show;
    }

    public String getCurrentProgram() {
        return currentProgram;
    }

    public void setCurrentProgram(String currentProgram) {
        this.currentProgram = currentProgram;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDel() {
        return del;
    }

    public void setDel(String del) {
        this.del = del;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getProgramTimePeriod() {
        return programTimePeriod;
    }

    public void setProgramTimePeriod(String programTimePeriod) {
        this.programTimePeriod = programTimePeriod;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getNumberOfListener() {
        return numberOfListener;
    }

    public void setNumberOfListener(String numberOfListener) {
        this.numberOfListener = numberOfListener;
    }

    public String convertToJsonString() {
        JSONObject object = new JSONObject();
        try {
            object.put(MediaField.rowKey, id);
            object.put(MediaField.FM, mediaFM);
            object.put(MediaField.type, mediaType);
            object.put(MediaField.tag, mediaTag);
            object.put(MediaField.currentProgramId, currentProgramId);
            object.put(MediaField.show, show);
            object.put(MediaField.currentProgram, currentProgram);
            object.put(MediaField.notes, notes);
            object.put(MediaField.del, del);
            object.put(MediaField.url, mediaUrl);
            object.put(MediaField.peroid, programTimePeriod);
            object.put(MediaField.icon, icon);
            object.put(MediaField.name, name);
            object.put(MediaField.listener, numberOfListener);
            object.put(MediaField.mediaId, mediaId);
            object.put(MediaField.mediaTimestamp, mediaTimestamp);
            object.put(MediaField.mediaDynamicUrl, mediaDynamicUrl);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return object.toString();
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaTimestamp() {
        return mediaTimestamp;
    }

    public void setMediaTimestamp(String mediaTimestamp) {
        this.mediaTimestamp = mediaTimestamp;
    }

    public String getMediaDynamicUrl() {
        return mediaDynamicUrl;
    }

    public void setMediaDynamicUrl(String mediaDynamicUrl) {
        this.mediaDynamicUrl = mediaDynamicUrl;
    }

    public String getMediaFM() {
        return mediaFM;
    }

    public void setMediaFM(String mediaFM) {
        this.mediaFM = mediaFM;
    }


}
