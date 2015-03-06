package com.java.onea.inus.event.subscribe;

import com.java.onea.inus.annotation.Subscribe;
import com.java.onea.inus.event.BlindEvent;

public interface BlindEventListener {

    @Subscribe
    public void onBlindEvent(BlindEvent blindEvent);

}
