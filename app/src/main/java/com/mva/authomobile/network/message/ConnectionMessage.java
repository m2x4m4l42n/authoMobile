package com.mva.authomobile.network.message;

import java.io.Serializable;

public abstract class ConnectionMessage implements Serializable {
   public abstract int getMessageType();
}
