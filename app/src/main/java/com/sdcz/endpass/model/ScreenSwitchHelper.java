package com.sdcz.endpass.model;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.Utils;
import com.sdcz.endpass.LiveDataBus;

import java.io.PrintWriter;

/**
 * 横竖屏切换的帮助类。
 * 通过监听重力传感器的变化，进行横竖屏的判断。
 */

public class ScreenSwitchHelper {
    private static final String TAG = "ScreenSwitchHelper";
    private final SensorManager sensorManager;
    private final Sensor sensor;
    private SensorEventListenerImpl sensorEventListener;
    private MutableLiveData<Integer> orientationLivedata;

    /**
     * 构造函数
     */
    public ScreenSwitchHelper() {
        sensorManager = (SensorManager) Utils.getApp().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    /**
     * 启动监听
     */
    public void startScreenSwitchListener() {
        this.orientationLivedata = LiveDataBus.getInstance().getLiveData(LiveDataBus.KEY_ORIENTATION);
        if (sensorEventListener == null) {
            sensorEventListener = new SensorEventListenerImpl();
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * 停止监听
     */
    public void stopScreenSwitchListener() {
        sensorManager.unregisterListener(sensorEventListener);
        sensorEventListener = null;
    }

    public int getSavedOrientation() {
        return sensorEventListener.savedOrientation;
    }

    /**
     * 重力感应监听者
     */
    private class OrientationSensorListener implements SensorEventListener {
        private static final int DATA_X = 0;
        private static final int DATA_Y = 1;
        private static final int DATA_Z = 2;

        private final long lastTime = 0;
        private final float oneEightyOverPi = 57.29577957855f; // 180/3.14
        private int lastOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;

            int orientationAngle;
            float xpos = -values[DATA_X];
            float ypos = -values[DATA_Y];
            float zpos = -values[DATA_Z];
            float magnitude = xpos * xpos + ypos * ypos;

            // Don't trust the angle if the magnitude is small compared to the y value
            if (magnitude * 4 >= zpos * zpos) {
                // 屏幕旋转时
                float angle = (float) Math.atan2(-ypos, xpos) * oneEightyOverPi;
                orientationAngle = 90 - Math.round(angle);
                // normalize to 0 - 359 range
                while (orientationAngle >= 360) {
                    orientationAngle -= 360;
                }

                while (orientationAngle < 0) {
                    orientationAngle += 360;
                }

                Log.i(TAG, "orientation = " + orientationAngle);

                onOrientationChanged(orientationAngle);
            }
        }

        private void onOrientationChanged(int orientationAngle) {
                // 正方向竖屏，倒方向的竖屏不响应
                boolean isPortrait = (orientationAngle > 330 || orientationAngle < 30);
                if (isPortrait && lastOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    lastOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    orientationLivedata.postValue(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    return;
                }

                // 向右横屏
                boolean isRightLandscape = orientationAngle > 60 && orientationAngle < 120;
                if (isRightLandscape && lastOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                    lastOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    orientationLivedata.postValue(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    return;
                }

                // 向左横屏
                boolean isLeftLandscape = orientationAngle > 240 && orientationAngle < 300;
                if (isLeftLandscape && lastOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    lastOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    orientationLivedata.postValue(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
        }
    }

    /**
     * This class filters the raw accelerometer data and tries to detect actual changes in
     * orientation. This is a very ill-defined problem so there are a lot of tweakable parameters,
     * but here's the outline:
     *
     * <p>- Low-pass filter the accelerometer vector in cartesian coordinates.  We do it in
     * cartesian space because the orientation calculations are sensitive to the
     * absolute magnitude of the acceleration.  In particular, there are singularities
     * in the calculation as the magnitude approaches 0.  By performing the low-pass
     * filtering early, we can eliminate most spurious high-frequency impulses due to noise.
     *
     * <p>- Convert the acceleromter vector from cartesian to spherical coordinates.
     * Since we're dealing with rotation of the device, this is the sensible coordinate
     * system to work in.  The zenith direction is the Z-axis, the direction the screen
     * is facing.  The radial distance is referred to as the magnitude below.
     * The elevation angle is referred to as the "tilt" below.
     * The azimuth angle is referred to as the "orientation" below (and the azimuth axis is
     * the Y-axis).
     * See http://en.wikipedia.org/wiki/Spherical_coordinate_system for reference.
     *
     * <p>- If the tilt angle is too close to horizontal (near 90 or -90 degrees), do nothing.
     * The orientation angle is not meaningful when the device is nearly horizontal.
     * The tilt angle thresholds are set differently for each orientation and different
     * limits are applied when the device is facing down as opposed to when it is facing
     * forward or facing up.
     *
     * <p>- When the orientation angle reaches a certain threshold, consider transitioning
     * to the corresponding orientation.  These thresholds have some hysteresis built-in
     * to avoid oscillations between adjacent orientations.
     *
     * <p>- Wait for the device to settle for a little bit.  Once that happens, issue the
     * new orientation proposal.
     *
     * <p>Details are explained inline.
     *
     * <p>See http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization for
     * signal processing background.
     */
    public class SensorEventListenerImpl implements SensorEventListener {
        private static final int ORIENTATION_PORTRAIT = 0;
        private static final int ORIENTATION_LANDSCAPE = 1;
        private static final int ORIENTATION_REVERSE_PORTRAIT = 2;
        private static final int ORIENTATION_REVERSE_LANDSCAPE = 3;

        // 倾斜角度的缓冲值， 只有当倾斜角度为直角一半的临界值 + 缓冲值才做方向变更的切换
        private static final int ANGLE_BUFFER = 10;

        // We work with all angles in degrees in this class.
        private static final float RADIANS_TO_DEGREES = (float) (180 / Math.PI);

        // Number of nanoseconds per millisecond.
        private static final long NANOS_PER_MS = 1000000;

        // Indices into SensorEvent.values for the accelerometer sensor.
        private static final int ACCELEROMETER_DATA_X = 0;
        private static final int ACCELEROMETER_DATA_Y = 1;
        private static final int ACCELEROMETER_DATA_Z = 2;

        // The minimum amount of time that a predicted rotation must be stable before it
        // is accepted as a valid rotation proposal.  This value can be quite small because
        // the low-pass filter already suppresses most of the noise so we're really just
        // looking for quick confirmation that the last few samples are in agreement as to
        // the desired orientation.
        private static final long PROPOSAL_SETTLE_TIME_NANOS = 40 * NANOS_PER_MS;

        // The minimum amount of time that must have elapsed since the device last exited
        // the flat state (time since it was picked up) before the proposed rotation
        // can change.
        private static final long PROPOSAL_MIN_TIME_SINCE_FLAT_ENDED_NANOS = 500 * NANOS_PER_MS;

        // The minimum amount of time that must have elapsed since the device stopped
        // swinging (time since device appeared to be in the process of being put down
        // or put away into a pocket) before the proposed rotation can change.
        private static final long PROPOSAL_MIN_TIME_SINCE_SWING_ENDED_NANOS = 300 * NANOS_PER_MS;

        // The minimum amount of time that must have elapsed since the device stopped
        // undergoing external acceleration before the proposed rotation can change.
        private static final long PROPOSAL_MIN_TIME_SINCE_ACCELERATION_ENDED_NANOS =
                500 * NANOS_PER_MS;

        // If the tilt angle remains greater than the specified angle for a minimum of
        // the specified time, then the device is deemed to be lying flat
        // (just chillin' on a table).
        private static final float FLAT_ANGLE = 75;
        private static final long FLAT_TIME_NANOS = 1000 * NANOS_PER_MS;

        // If the tilt angle has increased by at least delta degrees within the specified amount
        // of time, then the device is deemed to be swinging away from the user
        // down towards flat (tilt = 90).
        private static final float SWING_AWAY_ANGLE_DELTA = 20;
        private static final long SWING_TIME_NANOS = 300 * NANOS_PER_MS;

        // The maximum sample inter-arrival time in milliseconds.
        // If the acceleration samples are further apart than this amount in time, we reset the
        // state of the low-pass filter and orientation properties.  This helps to handle
        // boundary conditions when the device is turned on, wakes from suspend or there is
        // a significant gap in samples.
        private static final long MAX_FILTER_DELTA_TIME_NANOS = 1000 * NANOS_PER_MS;

        // The acceleration filter time constant.
        //
        // This time constant is used to tune the acceleration filter such that
        // impulses and vibrational noise (think car dock) is suppressed before we
        // try to calculate the tilt and orientation angles.
        //
        // The filter time constant is related to the filter cutoff frequency, which is the
        // frequency at which signals are attenuated by 3dB (half the passband power).
        // Each successive octave beyond this frequency is attenuated by an additional 6dB.
        //
        // Given a time constant t in seconds, the filter cutoff frequency Fc in Hertz
        // is given by Fc = 1 / (2pi * t).
        //
        // The higher the time constant, the lower the cutoff frequency, so more noise
        // will be suppressed.
        //
        // Filtering adds latency proportional the time constant (inversely proportional
        // to the cutoff frequency) so we don't want to make the time constant too
        // large or we can lose responsiveness.  Likewise we don't want to make it too
        // small or we do a poor job suppressing acceleration spikes.
        // Empirically, 100ms seems to be too small and 500ms is too large.
        private static final float FILTER_TIME_CONSTANT_MS = 150.0f;

        /* State for orientation detection. */

        // Thresholds for minimum and maximum allowable deviation from gravity.
        //
        // If the device is undergoing external acceleration (being bumped, in a car
        // that is turning around a corner or a plane taking off) then the magnitude
        // may be substantially more or less than gravity.  This can skew our orientation
        // detection by making us think that up is pointed in a different direction.
        //
        // Conversely, if the device is in freefall, then there will be no gravity to
        // measure at all.  This is problematic because we cannot detect the orientation
        // without gravity to tell us which way is up.  A magnitude near 0 produces
        // singularities in the tilt and orientation calculations.
        //
        // In both cases, we postpone choosing an orientation.
        //
        // However, we need to tolerate some acceleration because the angular momentum
        // of turning the device can skew the observed acceleration for a short period of time.
        private static final float NEAR_ZERO_MAGNITUDE = 1; // m/s^2
        private static final float ACCELERATION_TOLERANCE = 4; // m/s^2
        private static final float MIN_ACCELERATION_MAGNITUDE =
                SensorManager.STANDARD_GRAVITY - ACCELERATION_TOLERANCE;
        private static final float MAX_ACCELERATION_MAGNITUDE =
                SensorManager.STANDARD_GRAVITY + ACCELERATION_TOLERANCE;

        // Maximum absolute tilt angle at which to consider orientation data.  Beyond this (i.e.
        // when screen is facing the sky or ground), we completely ignore orientation data.
        private static final int MAX_TILT = 75;

        // The tilt angle range in degrees for each orientation.
        // Beyond these tilt angles, we don't even consider transitioning into the
        // specified orientation.  We place more stringent requirements on unnatural
        // orientations than natural ones to make it less likely to accidentally transition
        // into those states.
        // The first value of each pair is negative so it applies a limit when the device is
        // facing down (overhead reading in bed).
        // The second value of each pair is positive so it applies a limit when the device is
        // facing up (resting on a table).
        // The ideal tilt angle is 0 (when the device is vertical) so the limits establish
        // how close to vertical the device must be in order to change orientation.
        private final int[][] tiltTolerance = new int[][]{
                /* ROTATION_0   */ {-25, 70},
                /* ROTATION_90  */ {-25, 65},
                /* ROTATION_180 */ {-25, 60},
                /* ROTATION_270 */ {-25, 65}
        };

        // The tilt angle below which we conclude that the user is holding the device
        // overhead reading in bed and lock into that state.
        private final int tiltOverheadEnter = -40;

        // The tilt angle above which we conclude that the user would like a rotation
        // change to occur and unlock from the overhead state.
        private final int tiltOverheadExit = -15;

        // The gap angle in degrees between adjacent orientation angles for hysteresis.
        // This creates a "dead zone" between the current orientation and a proposed
        // adjacent orientation.  No orientation proposal is made when the orientation
        // angle is within the gap between the current orientation and the adjacent
        // orientation.
        private static final int ADJACENT_ORIENTATION_ANGLE_GAP = 45;

        // Timestamp and value of the last accelerometer sample.
        private long lastFilteredTimestampNanos;
        private float lastFilteredX;
        private float lastFilteredY;
        private float lastFilteredZ;

        // The last proposed rotation, -1 if unknown.
        private int proposedRotation;

        // Value of the current predicted rotation, -1 if unknown.
        private int predictedRotation;

        // Timestamp of when the predicted rotation most recently changed.
        private long predictedRotationTimestampNanos;

        // Timestamp when the device last appeared to be flat for sure (the flat delay elapsed).
        private long flatTimestampNanos;
        private boolean flat;

        // Timestamp when the device last appeared to be swinging.
        private long swingTimestampNanos;
        private boolean swinging;

        // Timestamp when the device last appeared to be undergoing external acceleration.
        private long accelerationTimestampNanos;
        private boolean accelerating;

        // Whether we are locked into an overhead usage mode.
        private boolean overhead;

        // History of observed tilt angles.
        private static final int TILT_HISTORY_SIZE = 40;
        private final float[] tiltHistory = new float[TILT_HISTORY_SIZE];
        private final long[] tiltHistoryTimestampNanos = new long[TILT_HISTORY_SIZE];
        private int tiltHistoryIndex;

        private final Object lock = new Object();
        private final boolean debug = false;
        private final int currentRotation = -1;
        // 保存上一次旋转的方法，该方向是与ActivityInfo中的方向，而不是求出来的【0-4】
        private int savedOrientation = -1;

        // 保存倾斜角度，用于测试打印角度
        private int savedAngle = -1;

        public int getProposedRotationLocked() {
            return proposedRotation;
        }

        /**
         * 打印日志
         */
        public void dumpLocked(PrintWriter pw, String prefix) {
            pw.println(prefix + "proposedRotation=" + proposedRotation);
            pw.println(prefix + "predictedRotation=" + predictedRotation);
            pw.println(prefix + "lastFilteredX=" + lastFilteredX);
            pw.println(prefix + "lastFilteredY=" + lastFilteredY);
            pw.println(prefix + "lastFilteredZ=" + lastFilteredZ);
            pw.println(prefix + "tiltHistory={last: " + getLastTiltLocked() + "}");
            pw.println(prefix + "flat=" + flat);
            pw.println(prefix + "swinging=" + swinging);
            pw.println(prefix + "accelerating=" + accelerating);
            pw.println(prefix + "overhead=" + overhead);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            int proposedRotation;
            int oldProposedRotation;

            synchronized (lock) {
                // The vector given in the SensorEvent points straight up (towards the sky) under
                // ideal conditions (the phone is not accelerating).  I'll call this up vector
                // elsewhere.
                float posX = event.values[ACCELEROMETER_DATA_X];
                float posY = event.values[ACCELEROMETER_DATA_Y];
                float posZ = event.values[ACCELEROMETER_DATA_Z];

                if (debug) {
                    Log.i(TAG, "Raw acceleration vector: "
                            + "x=" + posX + ", y=" + posY + ", z=" + posZ
                            + ", magnitude=" + Math.sqrt(posX * posX + posY * posY + posZ * posZ));
                }

                // Apply a low-pass filter to the acceleration up vector in cartesian space.
                // Reset the orientation listener state if the samples are too far apart in time
                // or when we see values of (0, 0, 0) which indicates that we polled the
                // accelerometer too soon after turning it on and we don't have any data yet.
                final long now = event.timestamp;
                final long then = lastFilteredTimestampNanos;
                final float timeDeltaMS = (now - then) * 0.000001f;
                final boolean skipSample;
                if (now < then
                        || now > then + MAX_FILTER_DELTA_TIME_NANOS
                        || (posX == 0 && posY == 0 && posZ == 0)) {
                    resetLocked();
                    skipSample = true;
                } else {
                    final float alpha = timeDeltaMS / (FILTER_TIME_CONSTANT_MS + timeDeltaMS);
                    posX = alpha * (posX - lastFilteredX) + lastFilteredX;
                    posY = alpha * (posY - lastFilteredY) + lastFilteredY;
                    posZ = alpha * (posZ - lastFilteredZ) + lastFilteredZ;
                    if (debug) {
                        Log.i(TAG, "Filtered acceleration vector: "
                                + "x=" + posX + ", y=" + posY + ", z=" + posZ
                                + ", magnitude=" + Math.sqrt(posX * posX + posY * posY + posZ * posZ));
                    }
                    skipSample = false;
                }
                lastFilteredTimestampNanos = now;
                lastFilteredX = posX;
                lastFilteredY = posY;
                lastFilteredZ = posZ;

                boolean isAccelerating = false;
                boolean isFlat = false;
                boolean isSwinging = false;
                if (!skipSample) {
                    // Calculate the magnitude of the acceleration vector.
                    final float magnitude = (float) Math.sqrt(posX * posX + posY * posY + posZ * posZ);
                    if (magnitude < NEAR_ZERO_MAGNITUDE) {
                        if (debug) {
                            Log.i(TAG, "Ignoring sensor data, magnitude too close to zero.");
                        }
                        clearPredictedRotationLocked();
                    } else {
                        // Determine whether the device appears to be undergoing external
                        // acceleration.
                        if (isAcceleratingLocked(magnitude)) {
                            isAccelerating = true;
                            accelerationTimestampNanos = now;
                        }

                        // Calculate the tilt angle.
                        // This is the angle between the up vector and the x-y plane (the plane of
                        // the screen) in a range of [-90, 90] degrees.
                        //   -90 degrees: screen horizontal and facing the ground (overhead)
                        //     0 degrees: screen vertical
                        //    90 degrees: screen horizontal and facing the sky (on table)
                        final int tiltAngle = (int) Math.round(
                                Math.asin(posZ / magnitude) * RADIANS_TO_DEGREES);
                        addTiltHistoryEntryLocked(now, tiltAngle);

                        // Determine whether the device appears to be flat or swinging.
                        if (isFlatLocked(now)) {
                            isFlat = true;
                            flatTimestampNanos = now;
                        }
                        if (isSwingingLocked(now, tiltAngle)) {
                            isSwinging = true;
                            swingTimestampNanos = now;
                        }

                        // If the tilt angle is too close to horizontal then we cannot determine
                        // the orientation angle of the screen.
                        if (tiltAngle <= tiltOverheadEnter) {
                            overhead = true;
                        } else if (tiltAngle >= tiltOverheadExit) {
                            overhead = false;
                        }
                        if (overhead) {
                            if (debug) {
                                Log.i(TAG, "Ignoring sensor data, device is overhead: "
                                        + "tiltAngle=" + tiltAngle);
                            }
                            clearPredictedRotationLocked();
                        } else if (Math.abs(tiltAngle) > MAX_TILT) {
                            if (debug) {
                                Log.i(TAG, "Ignoring sensor data, tilt angle too high: "
                                        + "tiltAngle=" + tiltAngle);
                            }
                            clearPredictedRotationLocked();
                        } else {
                            // Calculate the orientation angle.
                            // This is the angle between the x-y projection of the up vector onto
                            // the +y-axis, increasing clockwise in a range of [0, 360] degrees.
                            int orientationAngle = (int) Math.round(
                                    -Math.atan2(-posX, posY) * RADIANS_TO_DEGREES);
                            if (orientationAngle < 0) {
                                // atan2 returns [-180, 180]; normalize to [0, 360]
                                orientationAngle += 360;
                            }

                            // Find the nearest rotation.
                            int nearestRotation = covertAngleToOrientation(orientationAngle);

                            // Determine the predicted orientation.
                            if (isTiltAngleAcceptableLocked(nearestRotation, tiltAngle)
                                    && isOrientationAngleAcceptableLocked(nearestRotation,
                                    orientationAngle)) {
                                updatePredictedRotationLocked(now, nearestRotation);
                                if (debug) {
                                    Log.i(TAG, "Predicted: "
                                            + "tiltAngle=" + tiltAngle
                                            + ", orientationAngle=" + orientationAngle
                                            + ", predictedRotation=" + predictedRotation
                                            + ", predictedRotationAgeMS="
                                            + ((now - predictedRotationTimestampNanos)
                                            * 0.000001f));
                                }
                            } else {
                                if (debug) {
                                    Log.i(TAG, "Ignoring sensor data, no predicted rotation: "
                                            + "tiltAngle=" + tiltAngle
                                            + ", orientationAngle=" + orientationAngle);
                                }
                                clearPredictedRotationLocked();
                            }
                        }
                    }
                }
                flat = isFlat;
                swinging = isSwinging;
                accelerating = isAccelerating;

                // Determine new proposed rotation.
                oldProposedRotation = this.proposedRotation;
                if (predictedRotation < 0 || isPredictedRotationAcceptableLocked(now)) {
                    this.proposedRotation = predictedRotation;
                }
                proposedRotation = this.proposedRotation;

                // Write final statistics about where we are in the orientation detection process.
                if (debug) {
                    Log.i(TAG, "Result: currentRotation=" + currentRotation
                            + ", proposedRotation=" + proposedRotation
                            + ", predictedRotation=" + predictedRotation
                            + ", timeDeltaMS=" + timeDeltaMS
                            + ", isAccelerating=" + isAccelerating
                            + ", isFlat=" + isFlat
                            + ", isSwinging=" + isSwinging
                            + ", isOverhead=" + overhead
                            + ", timeUntilSettledMS=" + remainingMS(now,
                            predictedRotationTimestampNanos + PROPOSAL_SETTLE_TIME_NANOS)
                            + ", timeUntilAccelerationDelayExpiredMS=" + remainingMS(now,
                            accelerationTimestampNanos
                                    + PROPOSAL_MIN_TIME_SINCE_ACCELERATION_ENDED_NANOS)
                            + ", timeUntilFlatDelayExpiredMS=" + remainingMS(now,
                            flatTimestampNanos
                                    + PROPOSAL_MIN_TIME_SINCE_FLAT_ENDED_NANOS)
                            + ", timeUntilSwingDelayExpiredMS=" + remainingMS(now,
                            swingTimestampNanos
                                    + PROPOSAL_MIN_TIME_SINCE_SWING_ENDED_NANOS));
                }
            }

            // Tell the listener.
            if (proposedRotation != oldProposedRotation && proposedRotation >= 0) {
                if (debug) {
                    Log.i(TAG, "Proposed rotation changed!  proposedRotation=" + proposedRotation
                            + ", oldProposedRotation=" + oldProposedRotation);
                }

                // 如果是倒转的方向则将方向设置为上一个方向，目前不响应倒竖
                if (proposedRotation == ORIENTATION_REVERSE_PORTRAIT) {
                    this.proposedRotation = oldProposedRotation;
                    return;
                }

                    final int curOrientation = covertProposedToActivityRotation(proposedRotation);
                    if (curOrientation != savedOrientation) {
                        Log.i(TAG, "curAngle = " + savedAngle);
                        savedOrientation = curOrientation;
                        orientationLivedata.postValue(curOrientation);
                    }
            }
        }

        private int covertProposedToActivityRotation(int proposedRotation) {
            int rotation;
            switch (proposedRotation) {
                case ORIENTATION_PORTRAIT:
                    rotation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;

                case ORIENTATION_LANDSCAPE:
                    rotation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;

                case ORIENTATION_REVERSE_PORTRAIT:
                    rotation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;

                case ORIENTATION_REVERSE_LANDSCAPE:
                    rotation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;

                default:
                    rotation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }

            return rotation;
        }

        /**
         * 进行角度的转换，中间预留10度的缓冲区，角度计算是逆时针方向
         */
        private int covertAngleToOrientation(int angle) {
            if (angle >= 315 + ANGLE_BUFFER || angle < 45 - ANGLE_BUFFER) {
                return ORIENTATION_PORTRAIT;
            }

            if (angle >= 45 + ANGLE_BUFFER && angle < 135 - ANGLE_BUFFER) {
                return ORIENTATION_LANDSCAPE;
            }

            if (angle >= 135 + ANGLE_BUFFER && angle < 225 - ANGLE_BUFFER) {
                return ORIENTATION_REVERSE_PORTRAIT;
            }

            if (angle >= 225 + ANGLE_BUFFER && angle < 315 - ANGLE_BUFFER) {
                return ORIENTATION_REVERSE_LANDSCAPE;
            }

            savedAngle = angle;
            return proposedRotation != -1 ? proposedRotation : ORIENTATION_PORTRAIT;
        }

        /**
         * Returns true if the tilt angle is acceptable for a given predicted rotation.
         */
        private boolean isTiltAngleAcceptableLocked(int rotation, int tiltAngle) {
            return tiltAngle >= tiltTolerance[rotation][0]
                    && tiltAngle <= tiltTolerance[rotation][1];
        }

        /**
         * Returns true if the orientation angle is acceptable for a given predicted rotation.
         *
         * <p>This function takes into account the gap between adjacent orientations
         * for hysteresis.
         */
        private boolean isOrientationAngleAcceptableLocked(int rotation, int orientationAngle) {
            // If there is no current rotation, then there is no gap.
            // The gap is used only to introduce hysteresis among advertised orientation
            // changes to avoid flapping.
            final int currentRotation = this.currentRotation;
            if (currentRotation >= 0) {
                // If the specified rotation is the same or is counter-clockwise adjacent
                // to the current rotation, then we set a lower bound on the orientation angle.
                // For example, if currentRotation is ROTATION_0 and proposed is ROTATION_90,
                // then we want to check orientationAngle > 45 + GAP / 2.
                if (rotation == currentRotation
                        || rotation == (currentRotation + 1) % 4) {
                    int lowerBound = rotation * 90 - 45
                            + ADJACENT_ORIENTATION_ANGLE_GAP / 2;
                    if (rotation == 0) {
                        if (orientationAngle >= 315 && orientationAngle < lowerBound + 360) {
                            return false;
                        }
                    } else {
                        if (orientationAngle < lowerBound) {
                            return false;
                        }
                    }
                }

                // If the specified rotation is the same or is clockwise adjacent,
                // then we set an upper bound on the orientation angle.
                // For example, if currentRotation is ROTATION_0 and rotation is ROTATION_270,
                // then we want to check orientationAngle < 315 - GAP / 2.
                if (rotation == currentRotation
                        || rotation == (currentRotation + 3) % 4) {
                    int upperBound = rotation * 90 + 45
                            - ADJACENT_ORIENTATION_ANGLE_GAP / 2;
                    if (rotation == 0) {
                        return orientationAngle > 45 || orientationAngle <= upperBound;
                    } else {
                        return orientationAngle <= upperBound;
                    }
                }
            }
            return true;
        }

        /**
         * Returns true if the predicted rotation is ready to be advertised as a
         * proposed rotation.
         */
        private boolean isPredictedRotationAcceptableLocked(long now) {
            // The predicted rotation must have settled long enough.

            // Looks good!
            return true;
        }

        private void resetLocked() {
            lastFilteredTimestampNanos = Long.MIN_VALUE;
            proposedRotation = -1;
            flatTimestampNanos = Long.MIN_VALUE;
            flat = false;
            swingTimestampNanos = Long.MIN_VALUE;
            swinging = false;
            accelerationTimestampNanos = Long.MIN_VALUE;
            accelerating = false;
            overhead = false;
            clearPredictedRotationLocked();
            clearTiltHistoryLocked();
        }

        private void clearPredictedRotationLocked() {
            predictedRotation = -1;
            predictedRotationTimestampNanos = Long.MIN_VALUE;
        }

        private void updatePredictedRotationLocked(long now, int rotation) {
            if (predictedRotation != rotation) {
                predictedRotation = rotation;
                predictedRotationTimestampNanos = now;
            }
        }

        private boolean isAcceleratingLocked(float magnitude) {
            return magnitude < MIN_ACCELERATION_MAGNITUDE
                    || magnitude > MAX_ACCELERATION_MAGNITUDE;
        }

        private void clearTiltHistoryLocked() {
            tiltHistoryTimestampNanos[0] = Long.MIN_VALUE;
            tiltHistoryIndex = 1;
        }

        private void addTiltHistoryEntryLocked(long now, float tilt) {
            tiltHistory[tiltHistoryIndex] = tilt;
            tiltHistoryTimestampNanos[tiltHistoryIndex] = now;
            tiltHistoryIndex = (tiltHistoryIndex + 1) % TILT_HISTORY_SIZE;
            tiltHistoryTimestampNanos[tiltHistoryIndex] = Long.MIN_VALUE;
        }

        private boolean isFlatLocked(long now) {
            for (int i = tiltHistoryIndex; (i = nextTiltHistoryIndexLocked(i)) >= 0; ) {
                if (tiltHistory[i] < FLAT_ANGLE) {
                    break;
                }
                if (tiltHistoryTimestampNanos[i] + FLAT_TIME_NANOS <= now) {
                    // Tilt has remained greater than FLAT_TILT_ANGLE for FLAT_TIME_NANOS.
                    return true;
                }
            }
            return false;
        }

        private boolean isSwingingLocked(long now, float tilt) {
            for (int i = tiltHistoryIndex; (i = nextTiltHistoryIndexLocked(i)) >= 0; ) {
                if (tiltHistoryTimestampNanos[i] + SWING_TIME_NANOS < now) {
                    break;
                }
                if (tiltHistory[i] + SWING_AWAY_ANGLE_DELTA <= tilt) {
                    // Tilted away by SWING_AWAY_ANGLE_DELTA within SWING_TIME_NANOS.
                    return true;
                }
            }
            return false;
        }

        private int nextTiltHistoryIndexLocked(int index) {
            index = (index == 0 ? TILT_HISTORY_SIZE : index) - 1;
            return tiltHistoryTimestampNanos[index] != Long.MIN_VALUE ? index : -1;
        }

        private float getLastTiltLocked() {
            int index = nextTiltHistoryIndexLocked(tiltHistoryIndex);
            return index >= 0 ? tiltHistory[index] : Float.NaN;
        }

        private float remainingMS(long now, long until) {
            return now >= until ? 0 : (until - now) * 0.000001f;
        }
    }
}
