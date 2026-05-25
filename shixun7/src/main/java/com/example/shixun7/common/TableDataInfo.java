package com.example.shixun7.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class TableDataInfo implements Serializable {
    private long total;
    private List<?> rows;
    private int code;
    private String msg;
    private long timestamp;

    public TableDataInfo(List<?> list, long total) {
        this.rows = list;
        this.total = total;
        this.code = 200;
        this.msg = "success";
        this.timestamp = System.currentTimeMillis();
    }
}
