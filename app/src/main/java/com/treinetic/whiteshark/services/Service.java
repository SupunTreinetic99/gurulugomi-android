package com.treinetic.whiteshark.services;

import com.treinetic.whiteshark.exceptions.NetException;

public class Service {
    public interface Success<T> {
        public void success(T result);
    }

    public interface Error {
        public void error(NetException exception);
    }
}