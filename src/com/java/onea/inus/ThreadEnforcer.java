package com.java.onea.inus;

public interface ThreadEnforcer {

    public void enforce(EventBus bus);

    public static ThreadEnforcer ANY_THREAD = new ThreadEnforcer() {
        @Override
        public void enforce(EventBus bus) {
        }
    };

    public static ThreadEnforcer ONLY_MAIN_THREAD = new ThreadEnforcer() {
        @Override
        public void enforce(EventBus bus) {
            if (!Thread.currentThread().getName().contains("Main")) {
                throw new IllegalStateException("Event bus " + bus +
                        " accessed from non-main thread " + Thread.currentThread().getName());
            }
        }
    };

}
