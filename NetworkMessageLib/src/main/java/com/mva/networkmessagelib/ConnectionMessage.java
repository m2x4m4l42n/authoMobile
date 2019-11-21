package com.mva.networkmessagelib;

import java.io.Serializable;

public abstract class ConnectionMessage implements Serializable {
   public abstract int getMessageType();
}
