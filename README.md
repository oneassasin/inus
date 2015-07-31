inus - event bus by oneassasin
===
inus is a abbreviation to Interface Event Bus.

Based on [Otto by Square](http://square.github.io/otto/).

Usage
---

Use of the library is similar to [Otto](http://square.github.io/otto/), except that for each producer may be several Event.

###Event interface generation

Create a new event class and add ```@Event``` annotation.

    @Event
    public final class SomeEvent {
    
        public final int someField;
    
        public SomeEvent(int someField) {
            this.someField = someField;
        }
    
    }

After project rebuild, annotation processor generates interface with two suffixes: Producer and Listener.

    public interface SomeEventProducer {
    
        @Produce
        public SomeEvent produceSomeEvent();
        
    }
    
    public interface SomeEventListener {
    
            @Subscribe
            public void onSomeEvent();
            
    }
    
Implements his in your classes and using. 