package com.wanghui.livegesturedemo.adapter;

import android.app.Activity;
import android.graphics.RectF;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import com.wanghui.livegesturedemo.R;
import com.wanghui.livegesturedemo.Utils.IjkPlayerHelper;
import com.wanghui.livegesturedemo.Utils.LogUtil;
import com.wanghui.livegesturedemo.Utils.ScreenUtils;
import com.wanghui.livegesturedemo.databinding.ItemLiveRoomPagerBinding;
import com.wanghui.livegesturedemo.widget.HorizatialSlideFrameLayout;

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
    private ItemLiveRoomPagerBinding lastBinding;
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


    private boolean shouldIntercept;
    private float downX;
    private float downY;
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.item_live_room_pager, null, true);
        ItemLiveRoomPagerBinding mBind = ItemLiveRoomPagerBinding.bind(contentView);
        bindingList.add(mBind);
        mBind.ivCloseSmall.setOnClickListener(this);
        mBind.bimgSmallLiveSwitch.setOnClickListener(this);
        mBind.hslideflayoutLiveroom.setOrientation();
        mBind.hslideflayoutLiveroom.setPart(3);
        if (mBinding != null) {
            mBinding.dragLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    RectF rect = ScreenUtils.calcViewScreenLocation(mBinding.canDragLayout);
                    float x = event.getRawX();
                    float y = event.getRawY();
                    if (rect.contains(x, y) && mBinding.canDragLayout.getVisibility() == View.VISIBLE) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN://只有down在小播放屏时内才能触发拖动
                                shouldIntercept = true;
                                downX = event.getRawX();
                                downY = event.getRawY();
                                return false;
                            case MotionEvent.ACTION_MOVE:
                                if (shouldIntercept) {
                                    return false;
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                                float upX = event.getRawX();
                                float upY = event.getRawY();
                                boolean hasMove;
                                if (Math.abs(upX - downX) < 5 && Math.abs(upY - downY) < 5) {
                                    hasMove = false;
                                } else {
                                    hasMove = true;
                                }
                                if (shouldIntercept && !hasMove) {
                                    int visibility = mBinding.ivCloseSmall.getVisibility();
                                    if (visibility == VISIBLE) {
                                        mBinding.ivCloseSmall.setVisibility(INVISIBLE);
                                    } else {
                                        mBinding.ivCloseSmall.setVisibility(VISIBLE);
                                    }
                                }
                                LogUtil.i("LiveRoomNewPagerAdapter", "up事件");
                            case MotionEvent.ACTION_CANCEL:
                                if (shouldIntercept) {
                                    shouldIntercept = false;
                                    return false;
                                }
                                break;
                            default:
                                return false;

                        }
                    } else if (shouldIntercept) {//当down在小屏内时，隔绝外部事件的触发，尤其是当手指移动过快的时候事件会落到小屏外
                        return false;
                    }
                    if (shouldIntercept) {
                        return false;
                    } else {
                        LogUtil.i("LiveRoomNewPager", "触发事件穿透");
                        return mBinding.hslideflayoutLiveroom.dispatchTouchEvent(event);//当down没有在小屏内时，让事件穿透到下一层
                    }
                }
            });


        }

//        if (mBinding != null) {
//            mBinding.hslideflayoutLiveroom.setOnSlideFinishListening(new HorizatialSlideFrameLayout.OnSlideFinishListening() {
//                @Override
//                public void slidefinish(boolean isShow) {
//                    if (viewHolderUp != null) {
//                        viewHolderUp.mBind.hslideflayoutLiveroom.reShow(isShow, viewHolderUp.mBind.hslideflayoutLiveroom.getScrollX());
//                    }
//                    if (viewHolderDown != null) {
//                        viewHolderDown.mBind.hslideflayoutLiveroom.reShow(isShow, viewHolderDown.mBind.hslideflayoutLiveroom.getScrollX());
//                    }
////                    slidePresentTranslateXCallback(isShow);
//                }
//
//                @Override
//                public void onSlideScrolled(int offset) {
//                    slidePresentTranslateXCallback(offset);
//                }
//            });
//        }
        container.addView(contentView);

        return contentView;
    }

    public void play(int position) {
        mBinding = bindingList.get(position);

        if (ijkPlayerHelper == null) {

            ijkPlayerHelper = IjkPlayerHelper.getInstance();
        }
        if (ijkMediaPlayer != null) {
            ijkPlayerHelper.endPlayer(ijkMediaPlayer);
        }

        if (cameraPlayer != null) {
            ijkPlayerHelper.endPlayer(cameraPlayer);
        }


        ijkMediaPlayer = ijkPlayerHelper.open(dataList.get(position), mBinding.playerFullscreenSurfaceView.getHolder(), true);
        cameraPlayer = ijkPlayerHelper.openWithTextureView(dataList.get(position), new Surface(mBinding.surfaceCamera.getSurfaceTexture()));

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
