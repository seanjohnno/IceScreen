### Don't use yet...

View hiearchy isn't save/restoring at the moment

### What is it?

An alternative to Activity / Fragment based navigation for Android. 'Screens' are retained along with their member variables on a configuration change / rotation. The View belonging to the Screen is still re-created. It behaves like a retained fragment but without the complicated flow.

### Usage

Create a screen:

```java
public class ExampleScreen extends Screen {
    @Override
    protected View onCreateView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.your_view, null);
    }
}
```

Extend ScreenActivity and add your screen:

```java
public class ExampleActivity extends ScreenActivity {
    @Override
    protected void onInitialiseScreenManager(ScreenManager screenMan) {
        screenMan.pushScreen(new ExampleScreen());
    }
}
```

You can add more screens, just do it on the Main thread:

```java
  @Override
    protected View onCreateView(LayoutInflater inflater) {
        View v  = inflater.inflate(R.layout.your_view, null);
        v.findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
                getScreenManager().pushScreen(new Example2Screen());
            }
        });
    }
```
