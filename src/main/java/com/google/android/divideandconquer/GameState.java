package com.google.android.divideandconquer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class GameState implements Parcelable {
    /* package */ int numBalls;
    /* package */ int numLives;
    /* package */ int numLivesStart;
    /* package */ DivideAndConquerView.Mode mode;
    /* package */ DirectionPoint directionPoint;

    /* package */ List<BallRegion> newRegions;
    /* package */ List<BallRegion> regions;

    /* package */ GameState() {}

    protected GameState(Parcel in) {
        numBalls = in.readInt();
        numLives = in.readInt();
        numLivesStart = in.readInt();
        mode = DivideAndConquerView.Mode.valueOf(in.readString());
        directionPoint = in.readParcelable(null);
        newRegions = in.createTypedArrayList(BallRegion.CREATOR);
        regions = in.createTypedArrayList(BallRegion.CREATOR);
    }

    public static final Creator<GameState> CREATOR = new Creator<GameState>() {
        @Override
        public GameState createFromParcel(Parcel in) {
            return new GameState(in);
        }

        @Override
        public GameState[] newArray(int size) {
            return new GameState[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(numBalls);
        dest.writeInt(numLives);
        dest.writeInt(numLivesStart);
        dest.writeString(mode.toString());
        dest.writeParcelable(directionPoint, flags);
        dest.writeTypedList(newRegions);
        dest.writeTypedList(regions);
    }
}
