package com.wanghui.livegesturedemo.adapter;

import android.app.Activity;
import android.graphics.RectF;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import com.wanghui.livegesturedemo.R;
import com.wanghui.livegesturedemo.Utils.IjkPlayerHelper;
import com.wanghui.livegesturedemo.Utils.LogUtil;
import com.wanghui.livegesturedemo.Utils.ScreenUtils;
import com.wanghui.livegesturedemo.bean.LiveViewersPicBean;
import com.wanghui.livegesturedemo.databinding.ItemLiveRoomPagerBinding;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created by wangyubao123 on 2017/12/21.
 */

public class VerticalViewPagerAdapter extends PagerAdapter implements View.OnClickListener {
    private List<String> dataList;
    private Activity context;
    private ItemLiveRoomPagerBinding mBinding;
    private List<ItemLiveRoomPagerBinding> bindingList = new ArrayList<>();
    private IjkPlayerHelper ijkPlayerHelper;
    private IjkMediaPlayer ijkMediaPlayer;
    private IjkMediaPlayer cameraPlayer;

    public VerticalViewPagerAdapter(Activity context, List<String> dataList){
        this.dataList = dataList;
        this.context = context;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View contentView = (View) object;
        container.removeView(contentView);
    }

    private boolean isDownInSmall;//是否在小屏内
    private float downX;
    private float downY;
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.item_live_room_pager, null, true);
        final ItemLiveRoomPagerBinding mBind = ItemLiveRoomPagerBinding.bind(contentView);
        bindingList.add(mBind);
        mBind.ivCloseSmall.setOnClickListener(this);
        mBind.bimgSmallLiveSwitch.setOnClickListener(this);
        mBind.hslideflayoutLiveroom.setOrientation();
        mBind.hslideflayoutLiveroom.setPart(3);
        mBind.dragLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return handleGesture(event, mBind);
            }
        });

        LinearLayoutManager linearLayoutManagerVer = new LinearLayoutManager(context);
        mBind.hlistLiveroomViewersVer.setLayoutManager(linearLayoutManagerVer);
        mBind.hlistLiveroomViewersVer.setHasFixedSize(true);
        linearLayoutManagerVer.setOrientation(LinearLayoutManager.HORIZONTAL);
        mBind.hlistLiveroomViewersVer.setAdapter(new ViewsPicHorizontalRvAdapter(context, initViewerData()));

        container.addView(contentView);

        return contentView;
    }

    private int[] initViewerData() {
        int[] imgResArray = {
                R.mipmap.em_1,
                R.mipmap.em_2,
                R.mipmap.em_3,
                R.mipmap.em_4,
                R.mipmap.em_5,
                R.mipmap.em_6,
                R.mipmap.em_7,
                R.mipmap.em_8,
                R.mipmap.em_9,
                R.mipmap.em_10};
        return imgResArray;
    }

    private boolean handleGesture(MotionEvent event, ItemLiveRoomPagerBinding mBind) {
        RectF rect = ScreenUtils.calcViewScreenLocation(mBind.canDragLayout);
        float x = event.getRawX();
        float y = event.getRawY();
        if (rect.contains(x, y) && mBind.canDragLayout.getVisibility() == View.VISIBLE) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN://只有down在小播放屏时内才能触发拖动
                    isDownInSmall = true;
                    downX = event.getRawX();
                    downY = event.getRawY();
                    return false;
                case MotionEvent.ACTION_MOVE:
                    if (isDownInSmall) {
                        return false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    //自定义关闭小屏的点击事件
                    float upX = event.getRawX();
                    float upY = event.getRawY();
                    boolean hasMove;
                    hasMove = !(Math.abs(upX - downX) < 5 && Math.abs(upY - downY) < 5);
                    if (isDownInSmall && !hasMove) {
                        int visibility = mBind.ivCloseSmall.getVisibility();
                        if (visibility == VISIBLE) {
                            mBind.ivCloseSmall.setVisibility(INVISIBLE);
                        } else {
                            mBind.ivCloseSmall.setVisibility(VISIBLE);
                        }
                    }
                    LogUtil.i("LiveRoomNewPagerAdapter", "up事件");
                case MotionEvent.ACTION_CANCEL:
                    if (isDownInSmall) {
                        isDownInSmall = false;
                        return false;
                    }
                    break;
                default:
                    return false;

            }
        } else if (isDownInSmall) {//当down在小屏内时，隔绝外部事件的触发，尤其是当手指移动过快的时候事件会落到小屏外
            return false;
        }
        if (isDownInSmall) {
            return false;
        } else {
            LogUtil.i("LiveRoomNewPager", "触发事件穿透");
            return mBind.hslideflayoutLiveroom.dispatchTouchEvent(event);//当down没有在小屏内时，让事件穿透到下一层
        }
    }

    public void play(int position) {
        mBinding = bindingList.get(position);

        if (ijkPlayerHelper == null) {

            ijkPlayerHelper = IjkPlayerHelper.getInstance();
        }
        ijkPlayerHelper.endPlayer(ijkMediaPlayer);

        ijkPlayerHelper.endPlayer(cameraPlayer);

        if (mBinding.playerFullscreenSurfaceView != null && mBinding.surfaceCamera != null) {
            ijkMediaPlayer = ijkPlayerHelper.open(dataList.get(position), mBinding.playerFullscreenSurfaceView.getHolder(), true);
            cameraPlayer = ijkPlayerHelper.openWithTextureView(dataList.get(position), new Surface(mBinding.surfaceCamera.getSurfaceTexture()));
        }

    }

    private boolean isCameraLive = true;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close_small:
                mBinding.canDragLayout.setVisibility(GONE);

                v.setVisibility(INVISIBLE);
                mBinding.bimgSmallLiveSwitch.setVisibility(VISIBLE);
                isCameraLive = false;
                break;

            case R.id.bimg_small_live_switch:
                if (isCameraLive) {
                    isCameraLive = false;
                    mBinding.canDragLayout.setVisibility(GONE);
                    mBinding.bimgSmallLiveSwitch.setVisibility(VISIBLE);
                } else {
                    mBinding.canDragLayout.setVisibility(VISIBLE);
                    mBinding.bimgSmallLiveSwitch.setVisibility(GONE);
                    isCameraLive = true;
                }
                break;
        }
    }



}
