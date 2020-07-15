package com.hobot.sample.app.util;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/**
 * 循环使用的数据临时存储结构
 * <p>
 * 通过分离 需要绘制和重新加载的数据减少锁的使用 和 对象的重复创建 以提高效率
 *
 * <li>1. 使用{@link RecyclerQueue#fill(Object[])} 装载可以使用的可回收火箭对象(该数据由外部初始化)</li>
 * <li>2. 使用{@link RecyclerQueue#take()} 取走一个可以填充燃料的飞船 </li>
 * <li>3. 使用{@link RecyclerQueue#join(Object)} 将填充过燃料的飞船重新 归入编制</li>
 * <li>4. 使用{@link RecyclerQueue#launch()} 将填充过燃料的火箭 发射</li>
 * <li>5. 使用{@link RecyclerQueue#recycle(Object)} 回收发射过的火箭 重新使用</li>
 * <p>
 * 解释:
 * <li>火箭   : 可以重复使用的对象</li>
 * <li>燃料   : 用来填充的数据</li>
 * <li>发射   : 取出重新填充过数据的对象 进行 绘制</li>
 * <li>状态   : 初始化时，需要主动加载 一些已经create的对象</li>
 * <li>回收   : 将绘制过的数据 重新进行使用</li>
 * <li>编制   : 已经就绪可以绘制的数据</>
 *
 * @param <T> 泛型
 */
public class RecyclerQueue<T> {
    private static final String TAG = RecyclerQueue.class.getSimpleName();

    private Queue<T> mFilledShips = new ArrayDeque<>();     // 填满燃料的
    private Queue<T> mRecycledShips = new ArrayDeque<>();   // 已经回收的

    private final boolean debug = false;

    // 全部飞船的数量
    private int mSize = 0;

    // region Public Method

    public RecyclerQueue() {
    }

    public RecyclerQueue(T... ships) {
        fill(ships);
    }

    /**
     * 初始化时候 装载 已经new出来的飞船
     *
     * @param ships
     */
    public void fill(@NonNull T... ships) {
        if (debug)
            Log.d(TAG, "fill: ships size = [" + ships.length + "]");
        mSize = ships.length;
        mRecycledShips.addAll(Arrays.asList(ships));
    }

    /**
     * 取出 火箭 去填充燃料
     *
     * @return 如果库里没有缺油的火箭了 返回NULL
     */
    public T take() {
        if (debug)
            Log.d(TAG, "take: ");
        if (0 == mRecycledShips.size()) {
            Log.w(TAG, "take: recycle size = 0");
            return null;
        }
        // 从队列中取出最开始的数据
        return mRecycledShips.poll();
    }

    /**
     * 填满燃料的飞船 回归编制
     *
     * @param filled 填充完的数据
     * @return
     */
    public boolean join(@NonNull T filled) {
        if (debug)
            Log.d(TAG, "join() called with: filled = [" + filled + "]");

        // 向队列offer值，若不能填 重新加如回收的队列
        boolean rst = mFilledShips.offer(filled);
        if (!rst) {
            boolean rec = mRecycledShips.offer(filled);
            Log.w(TAG, "join: can not join recycle rst = [" + rec + "]");
        }
        return rst;
    }

    /**
     * 发射加满油的飞船
     *
     * @return 若没有加满燃料的 返回NULL
     */
    public T launch() {
        if (debug)
            Log.d(TAG, "launch() called");
        return mFilledShips.poll();
    }


    /**
     * 发射过的飞船 需要进行回收
     *
     * @param recycled 展示过的数据
     * @return 是否回收完成
     */
    public boolean recycle(@NonNull T recycled) {
        if (debug)
            Log.d(TAG, "recycle() called with: recycled = [" + recycled + "]");
        return mRecycledShips.add(recycled);
    }

    // endregion Public Method


    @Override
    public String toString() {
        return "filled = [" + mFilledShips.size() + "], " +
                "recycled = [" + mRecycledShips.size() + "]," +
                "total = [" + (mFilledShips.size() + mRecycledShips.size()) + "]," +
                "size = [" + mSize + "]";
    }
}
