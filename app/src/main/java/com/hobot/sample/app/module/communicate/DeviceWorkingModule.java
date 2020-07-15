package com.hobot.sample.app.module.communicate;

/**
 * Created by HP on 2018/5/9.
 * LDW_Level":3.0,
 * "LDW_Speed":60.0,
 * "HMW_Value":0.6,
 * "FCW_Value":2.7,
 * "DSA_Speed":5.0,
 * "DCA_Speed":5.0,
 * "Sharp_Acc":2.8,
 * "Sharp_Dec":-4.0,
 * "Sharp_Turn":3.0,
 * "Timer_Gap":60,
 * "Distance_Gap":1000,
 * "LDW_Enable":1,
 * "TWI_Enable":1,
 * "DSA_Enable":1,
 * "Timer_Enable":1,
 * "Distance_Enable":1
 *
 * @author Hobot
 */
public class DeviceWorkingModule {
    private int LDW_Level;
    private float LDW_Speed;
    private float HMW_Value;
    private float FCW_Value;
    private float DSA_Speed;
    private float DCA_Speed;
    private float Sharp_Acc;
    private float Sharp_Dec;
    private float Sharp_Turn;
    private int Timer_Gap;
    private int Distance_Gap;
    private int LDW_Enable;
    private int TWI_Enable;
    private int DSA_Enable;
    private int Timer_Enable;
    private int Distance_Enable;

    private DeviceWorkingModule(Builder builder) {
        LDW_Level = builder.LDW_Level;
        LDW_Speed = builder.LDW_Speed;
        HMW_Value = builder.HMW_Value;
        FCW_Value = builder.FCW_Value;
        DSA_Speed = builder.DSA_Speed;
        DCA_Speed = builder.DCA_Speed;
        Sharp_Acc = builder.Sharp_Acc;
        Sharp_Dec = builder.Sharp_Dec;
        Sharp_Turn = builder.Sharp_Turn;
        Timer_Gap = builder.Timer_Gap;
        Distance_Gap = builder.Distance_Gap;
        LDW_Enable = builder.LDW_Enable;
        TWI_Enable = builder.TWI_Enable;
        DSA_Enable = builder.DSA_Enable;
        Timer_Enable = builder.Timer_Enable;
        Distance_Enable = builder.Distance_Enable;
    }

    public int getLDW_Level() {
        return LDW_Level;
    }

    public float getLDW_Speed() {
        return LDW_Speed;
    }

    public float getHMW_Value() {
        return HMW_Value;
    }

    public float getFCW_Value() {
        return FCW_Value;
    }

    public float getDSA_Speed() {
        return DSA_Speed;
    }

    public float getDCA_Speed() {
        return DCA_Speed;
    }

    public float getSharp_Acc() {
        return Sharp_Acc;
    }

    public float getSharp_Dec() {
        return Sharp_Dec;
    }

    public float getSharp_Turn() {
        return Sharp_Turn;
    }

    public int getTimer_Gap() {
        return Timer_Gap;
    }

    public int getDistance_Gap() {
        return Distance_Gap;
    }

    public int getLDW_Enable() {
        return LDW_Enable;
    }

    public int getTWI_Enable() {
        return TWI_Enable;
    }

    public int getDSA_Enable() {
        return DSA_Enable;
    }

    public int getTimer_Enable() {
        return Timer_Enable;
    }

    public int getDistance_Enable() {
        return Distance_Enable;
    }

    public static class Builder {
        private int LDW_Level;
        private float LDW_Speed;
        private float HMW_Value;
        private float FCW_Value;
        private float DSA_Speed;
        private float DCA_Speed;
        private float Sharp_Acc;
        private float Sharp_Dec;
        private float Sharp_Turn;
        private int Timer_Gap;
        private int Distance_Gap;
        private int LDW_Enable;
        private int TWI_Enable;
        private int DSA_Enable;
        private int Timer_Enable;
        private int Distance_Enable;

        public Builder ldw_level(int LDW_Level) {
            this.LDW_Level = LDW_Level;
            return this;
        }

        public Builder ldw_speed(float LDW_Speed) {
            this.LDW_Speed = LDW_Speed;
            return this;
        }

        public Builder hmw_value(float HMW_Value) {
            this.HMW_Value = HMW_Value;
            return this;
        }

        public Builder fcw_value(float FCW_Value) {
            this.FCW_Value = FCW_Value;
            return this;
        }

        public Builder dsa_speed(float DSA_Speed) {
            this.DSA_Speed = DSA_Speed;
            return this;
        }

        public Builder dca_speed(float DCA_Speed) {
            this.DCA_Speed = DCA_Speed;
            return this;
        }

        public Builder sharp_acc(float Sharp_Acc) {
            this.Sharp_Acc = Sharp_Acc;
            return this;
        }

        public Builder sharp_dec(float Sharp_Dec) {
            this.Sharp_Dec = Sharp_Dec;
            return this;
        }

        public Builder sharp_turn(float Sharp_Turn) {
            this.Sharp_Turn = Sharp_Turn;
            return this;
        }

        public Builder timer_gap(int Timer_Gap) {
            this.Timer_Gap = Timer_Gap;
            return this;
        }

        public Builder distance_gap(int Distance_Gap) {
            this.Distance_Gap = Distance_Gap;
            return this;
        }

        public Builder ldw_enable(int LDW_Enable) {
            this.LDW_Enable = LDW_Enable;
            return this;
        }

        public Builder twi_enable(int TWI_Enable) {
            this.TWI_Enable = TWI_Enable;
            return this;
        }

        public Builder dsa_enable(int DSA_Enable) {
            this.DSA_Enable = DSA_Enable;
            return this;
        }

        public Builder timer_enable(int Timer_Enable) {
            this.Timer_Enable = Timer_Enable;
            return this;
        }

        public Builder distance_enable(int Distance_Enable) {
            this.Distance_Enable = Distance_Enable;
            return this;
        }

        public DeviceWorkingModule build() {
            return new DeviceWorkingModule(this);
        }
    }
}
