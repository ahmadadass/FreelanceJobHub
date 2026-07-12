package com.ucas.freelancejobhub.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/** Complete project entity passed between list, details, and edit screens. */
public class Project implements Parcelable {
    private long id;
    private long userId;
    private String title;
    private String clientName;
    private String clientPhone;
    private String meetingAddress;
    private double budget;
    private String status;
    private String dueDate;
    private String description;

    public Project() {
    }

    public Project(long id, long userId, String title, String clientName, String clientPhone,
                   String meetingAddress, double budget, String status, String dueDate,
                   String description) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.meetingAddress = meetingAddress;
        this.budget = budget;
        this.status = status;
        this.dueDate = dueDate;
        this.description = description;
    }

    protected Project(Parcel in) {
        id = in.readLong();
        userId = in.readLong();
        title = in.readString();
        clientName = in.readString();
        clientPhone = in.readString();
        meetingAddress = in.readString();
        budget = in.readDouble();
        status = in.readString();
        dueDate = in.readString();
        description = in.readString();
    }

    public static final Creator<Project> CREATOR = new Creator<Project>() {
        @Override
        public Project createFromParcel(Parcel in) {
            return new Project(in);
        }

        @Override
        public Project[] newArray(int size) {
            return new Project[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(userId);
        dest.writeString(title);
        dest.writeString(clientName);
        dest.writeString(clientPhone);
        dest.writeString(meetingAddress);
        dest.writeDouble(budget);
        dest.writeString(status);
        dest.writeString(dueDate);
        dest.writeString(description);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }
    public String getMeetingAddress() { return meetingAddress; }
    public void setMeetingAddress(String meetingAddress) { this.meetingAddress = meetingAddress; }
    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
