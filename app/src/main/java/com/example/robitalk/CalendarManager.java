package com.example.robitalk;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;

import java.util.List;




import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceSlotStatusData;

/**
 * Created by hiratsukamichihisa on 31/07/2015.
 */
public class CalendarManager {
    private Handler mHandler;

    /**
     * コンストラクタ
     */
    public CalendarManager(Handler handler) {
        mHandler = handler;
    }

    /**
     * メッセージ送信
     */
    private void sendMessage(Object obj, int arg2, int what) {
        Message msg = new Message();
        msg.obj = obj;
        msg.arg1 = Uti.CALENDAR;
        msg.arg2 = arg2;
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    /**
     *
     */
    public void start(List<SentenceSlotStatusData> slotStatusList) {

        Time time = convertSlotToTime(slotStatusList);

        sendMessage(time, Uti.API_NOTIFY_RESULT, 0);
    }

    private Time convertSlotToTime(List<SentenceSlotStatusData> slotStatusList) {

        // change request to start time
        Time time = new Time();
        time.setToNow();
        time.hour = 0;
        int nSlots = slotStatusList.size();
        if (nSlots != 0) {
            for (int i = 0; i < nSlots; i++) {
                SentenceSlotStatusData slot = slotStatusList.get(i);
                String slotName = slot.getSlotName();
                String slotValue = slot.getSlotValue();
                String valueType = slot.getValueType();
                if(valueType == null){
                    time.monthDay += 0;
                }
                else if (slotName.equals("date")) {
                    switch (valueType) {
                        case "date":
//                            time.monthDay = Integer.parseInt(slotValue);
                            break;
                        case "datePnoun":
                            if (slotValue.equals("今日")) time.monthDay += 0;
                            else if (slotValue.equals("明日")) time.monthDay += 1;
                            else if (slotValue.equals("明後日")) time.monthDay += 2;
                            break;
                        case "dateWeek":
                            int ith = 0;
                            if(slotValue.equals("月曜"))ith = 1;
                            if(slotValue.equals("火曜"))ith = 2;
                            if(slotValue.equals("水曜"))ith = 3;
                            if(slotValue.equals("木曜"))ith = 4;
                            if(slotValue.equals("金曜"))ith = 5;
                            if(slotValue.equals("土曜"))ith = 6;
//                            if(slotValue.equals("日曜"));
                            time.monthDay = time.monthDay + 7 + ith - time.weekDay;
                            time.weekDay = ith;
                            break;
                        case "dataRelative":
//                            time.monthDay += Integer.parseInt(slotValue);
                            time.monthDay += 0;
                            break;
                        case "none":
                            time.monthDay += 0;
                            break;
                        default:
                            time.monthDay += 0;
                            break;
                    }
                } else if (slotName.equals("time")) {

//                    int startTime = Integer.parseInt(slotValue);
//                    time.hour = startTime;
                }
            }
        }
        if (time.monthDay > 31 && seeLongOrNot(time.month) ) {
            time.monthDay -= 31;
            time.month += 1;
        }
        else if (time.monthDay > 30 && !seeLongOrNot(time.month) ) {
            time.monthDay -= 30;
            time.month += 1;
        }

        if(time.month > 12){
            time.month-= 12;
            time.year += 1;
        }
        return time;
    }

    public boolean seeLongOrNot(int month){
        month += 1;
        if(month == 2 || month == 4 || month == 6 || month == 9 || month == 11 ){
            return false;
        }
        return true;
    }

//    public int extractInt(String s){
//        int n = s.length();
//        String new_s;
//        for(int i=0;i<n;i++){
//            if(NumberUtils.IsDigits(s, i)){
//                new_s.
//
//            }
//        }
//
//    }

}
