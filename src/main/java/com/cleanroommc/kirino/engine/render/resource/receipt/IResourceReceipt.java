package com.cleanroommc.kirino.engine.render.resource.receipt;

import org.jspecify.annotations.NonNull;

public interface IResourceReceipt<T extends IResourceReceipt<T>> {
    @NonNull
    T getReceipt();
}
