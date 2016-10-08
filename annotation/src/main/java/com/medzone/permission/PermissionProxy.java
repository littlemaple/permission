package com.medzone.permission;

/**
 * Created by 44260 on 2016/9/30.
 */
public interface PermissionProxy<T> {
    void grant(T source, int requestCode);

    void denied(T source, int requestCode);

    void rationale(T source, int requestCode);

    boolean needShowRationale(int requestCode);
}
