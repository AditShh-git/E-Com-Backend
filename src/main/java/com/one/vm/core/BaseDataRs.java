package com.one.vm.core;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


// Responsible for return success/Failure message
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseDataRs implements Serializable {

    private String message;

    // ✅ New optional field to hold additional data (won’t affect old APIs)
    private Object data;

    public BaseDataRs(String message) {
        this.message = message;
    }

    // ✅ You can omit this if using Lombok (@Setter already adds it)
    public void setData(Object data) {
        this.data = data;
    }

}
