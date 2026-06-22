package edu.jxnu.microcoursebackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private boolean success;
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(true, 1, "success", data);
    }

    public static Result<Void> success() {
        return new Result<>(true, 1, "success", null);
    }

    public static Result<Void> error(String msg) {
        return new Result<>(false, 0, msg, null);
    }
}
