package com.example.clark.rxjava2;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.clark.rxjava2.lifecycle.LifecycleEvent;
import com.example.clark.rxjava2.lifecycle.LifecycleTransformer;

import java.text.SimpleDateFormat;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

/**
 * 测试RxJava绑定activity生命周期的方法
 */
public class LifecycleActivity extends AppCompatActivity implements View.OnClickListener {

    private final BehaviorSubject<LifecycleEvent> lifecycleSubject = BehaviorSubject.create();

    /**
     * 通过使用disposable来测试当disposable.dispose()的时候，会不会让Observable停止发射数据，结果：不行。
     */
    Disposable disposable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lifecycle);

        findViewById(R.id.btn_start).setOnClickListener(this);
        findViewById(R.id.btn_stop).setOnClickListener(this);
        findViewById(R.id.btn_oncreate).setOnClickListener(this);
        findViewById(R.id.btn_onstart).setOnClickListener(this);
        findViewById(R.id.btn_onresume).setOnClickListener(this);
        findViewById(R.id.btn_onpause).setOnClickListener(this);
        findViewById(R.id.btn_onstop).setOnClickListener(this);
        findViewById(R.id.btn_ondestory).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                disposable = Observable
                        .create(new ObservableOnSubscribe<String>() {
                            @Override
                            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                                while (true) {
                                    SystemClock.sleep(3000);
                                    String format = new SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(System.currentTimeMillis());
                                    String str = "===》发射的时间:" + format;
                                    Log.i("haha", str);
                                    emitter.onNext(format);
                                }
                            }
                        })
                        .map(new Function<String, String>() {
                            @Override
                            public String apply(String s) throws Exception {
                                return s;
                            }
                        })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(this.<String>getLifecycleTransformer(lifecycleSubject))
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
                                String str = "《===接收的时间:" + s;
                                Log.i("haha", str);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.i("haha", "Observer.onError()：" + throwable);
                            }
                        }, new Action() {
                            @Override
                            public void run() throws Exception {
                                Log.i("haha", "Observer.onComplete()");
                            }
                        }, new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {
                                Log.i("haha", "Observer.onSubscribe()");
                            }
                        });
                break;
            case R.id.btn_stop:
                if (disposable != null && !disposable.isDisposed()) {
                    disposable.dispose();
                }
                break;
            case R.id.btn_oncreate:
                lifecycleSubject.onNext(LifecycleEvent.ON_CREATE);
                break;
            case R.id.btn_onstart:
                lifecycleSubject.onNext(LifecycleEvent.ON_START);
                break;
            case R.id.btn_onresume:
                lifecycleSubject.onNext(LifecycleEvent.ON_RESUME);
                break;
            case R.id.btn_onpause:
                lifecycleSubject.onNext(LifecycleEvent.ON_PAUSE);
                break;
            case R.id.btn_onstop:
                lifecycleSubject.onNext(LifecycleEvent.ON_STOP);
                break;
            case R.id.btn_ondestory:
                lifecycleSubject.onNext(LifecycleEvent.ON_DESTORY);
                break;
            default:
                break;
        }
    }

    private <T> LifecycleTransformer<T> getLifecycleTransformer(final Observable<LifecycleEvent> lifecycle) {
        /**
         * 1.filter()：通过filter()方法返回一个过滤了LifecycleEvent.ON_DESTORY之外所有生命周期的Observable，
         *             只有接收到LifecycleEvent为ON_DESTROY的时候，才会发射数据LifecycleEvent.ON_DESTROY。
         * 2.takeUntil()：observable3 = observable1.takeUntil(observable2)，
         *                通过takeUntil()方法将会返回一个发射数据的observable3，
         *                这个observable3将会发射它的数据，直到observable2发射了一个数据（不管什么数据），observable3将会停止发射数据。
         * 3.可以看到，步骤1中所返回的Observable就是对应了步骤2中的observable2，所以当处于ON_DESTROY生命周期的时候，observable2发射了一个数据，断开了原始Observable的订阅管理。
         */
        Observable<LifecycleEvent> lifeIntercept = lifecycle.filter(new Predicate<LifecycleEvent>() {
            @Override
            public boolean test(LifecycleEvent lifecycleEvent) throws Exception {
                boolean result = lifecycleEvent.equals(LifecycleEvent.ON_DESTORY);
                //当result为false的时候，则不会发射消息；只有result为true的时候，才会发射数据。
                Log.i("haha", "订阅的生命周期：" + lifecycleEvent + "，Boolean：" + result);
                return result;
            }
        });
        return new LifecycleTransformer<>(lifeIntercept);
    }

}
