package com.isport.tracker.dialogActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.isport.tracker.R;

import java.util.concurrent.ExecutionException;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import jp.wasabeef.glide.transformations.CropSquareTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by Bravo  图片加载的基类。
 */
public class LoadImageUtil {

    private static LoadImageUtil instance;

    public static LoadImageUtil getInstance() {
        if (null == instance) {
            synchronized (LoadImageUtil.class) {
                if (null == instance) {
                    instance = new LoadImageUtil();
                }
            }
        }
        return instance;
    }

    public void load(Context ctx, String url, final ImageView iv) {
        if (ctx == null) {
            return;
        }
        Glide.with(ctx).load(url)
                .centerCrop()
                .placeholder(R.drawable.image_default_user_icon)
                .error(R.drawable.image_default_user_icon)
                .dontAnimate()
                /*   .skipMemoryCache(false)
                   .priority(Priority.LOW)
                   .diskCacheStrategy(DiskCacheStrategy.ALL)*/
                .into(iv);
               /* .into(new SimpleTarget<GlideDrawable>() { // 加上这段代码 可以解决
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable> glideAnimation) {
                        iv.setImageDrawable(resource); //显示图片
                    }
                });*/
    }

    public void load(Context ctx, String url, final ImageView iv, boolean zoom) {
        Glide.with(ctx).load(url)
                .placeholder(R.drawable.image_default_user_icon)
                .error(R.drawable.image_default_user_icon)
                .dontAnimate()
                /*   .skipMemoryCache(false)
                   .priority(Priority.LOW)
                   .diskCacheStrategy(DiskCacheStrategy.ALL)*/
                .into(iv);
               /* .into(new SimpleTarget<GlideDrawable>() { // 加上这段代码 可以解决
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable> glideAnimation) {
                        iv.setImageDrawable(resource); //显示图片
                    }
                });*/
    }

    /**
     * @Description: 加载本地文件方式
     */
    public static void displayImagePath(Context activity, String path, final ImageView imageView) {
        if (((Activity) activity).isFinishing())
            return;
        Glide.with(activity).
                load("file://" + path).
                centerCrop()
                .placeholder(R.drawable.image_default_user_icon).
                diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView);
    }


    public void load(Context ctx, String url, ImageView iv, int errorResId) {
        iv.setTag(iv.getId(), url);
        Glide.with(ctx)
                .load(url)
                .placeholder(R.drawable.image_default_user_icon)
                .error(errorResId)
                .centerCrop()
                .dontAnimate()
                .into(iv);
    }

    public void load(Context ctx, String url, ImageView iv, int placeholderResId, int errorResId) {
        Glide.with(ctx)
                .load(url)
                .centerCrop()
                .placeholder(placeholderResId)
                .dontAnimate()
                .error(errorResId)
                .into(iv);
    }

    public void loadWrapContent(Context ctx, String url, ImageView iv) {
        Glide.with(ctx)
                .load(url)
                .fitCenter()
                .into(iv);
    }

    public void loadRoundedRectangle(Context ctx, String url, ImageView iv, int radius, int margin) {
        /*Glide.with(ctx).load(url)
                .centerCrop()
                .placeholder(R.drawable.image_default_user_icon)
                .error(R.drawable.image_default_user_icon)
                .fitCenter()
                .bitmapTransform(new CropSquareTransformation(ctx), new RoundedCornersTransformation(ctx, radius, margin))
                .into(iv);*/
    }

    public void loadRound(Context ctx, String url, ImageView iv) {
      /*  Glide.with(ctx).load(url)
                .centerCrop()
                .placeholder(R.drawable.image_default_user_icon)
                .error(R.drawable.image_default_user_icon)
                .fitCenter()
                .bitmapTransform(new CropCircleTransformation(ctx))
                .into(iv);*/
    }

    public void loadRound(Context ctx, String url, ImageView iv, int errorResId) {
        /*Glide.with(ctx).load(url)
                .centerCrop()
                .placeholder(errorResId)
                .error(errorResId)
                .fitCenter()
                .bitmapTransform(new CropCircleTransformation(ctx))
                .into(iv);*/
    }

    public Bitmap getBitmap(Context ctx, String url) {
        return null;
       /* Bitmap bitmap = null;
        try {
            bitmap = Glide.with(ctx)
                    .load(url)
                    .asBitmap()
                    .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return bitmap;*/
    }

    public void loadGif(Context ctx, String url, final ImageView imageView) {
        //   Glide.with(ctx).load(url).into(new GlideDrawableImageViewTarget(imageView, 10)); //加载一次
       /* Glide.with(ctx).asGif().load(url).listener(new RequestListener() {
            @Override
            public boolean onLoadFailed(GlideException e, Object model, Target target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                if (resource instanceof GifDrawable) {
                    //加载一次
                    ((GifDrawable) resource).setLoopCount(1);
                }
                return false;
            }

        }).into(iv);*/
    }

    public void loadGif(Context ctx, int url, final ImageView imageView) {
        // Glide.with(ctx).load(url).into(new GlideDrawableImageViewTarget(imageView, 10)); //加载一次
       /* Glide.with(ctx).asGif().load(url).listener(new RequestListener() {
            @Override
            public boolean onLoadFailed(GlideException e, Object model, Target target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                if (resource instanceof GifDrawable) {
                    //加载一次
                    ((GifDrawable) resource).setLoopCount(1);
                }
                return false;
            }

        }).into(iv);*/
    }


    public void loadWithListener(Context ctx, String url, final LoadListener loadListener) {
       /* Glide.with(ctx).load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                loadListener.onResourceReady(resource, glideAnimation);
            }
        });*/
    }

    public interface LoadListener {
        // void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation);
    }
}