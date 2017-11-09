package com.firegroup.lanya;

/**
 * Created by Froze on 2017/10/28.
 */

public class CalculateTool {
    public byte Int2Byte(Integer integer){
        return (byte)(integer & 0xff);
    }
}
