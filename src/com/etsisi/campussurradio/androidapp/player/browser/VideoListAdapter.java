/*
 * Copyright 2019 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modified by Padrón Castañeda, Ruymán
 * Final degree work
 * ETSISI (UPM), Madrid 2019
 */

package com.etsisi.campussurradio.androidapp.player.browser;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.etsisi.campussurradio.androidapp.player.utils.CustomVolleyRequest;
import com.etsisi.campussurradio.androidapp.player.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * An {@link ArrayAdapter} to populate the list of videos.
 */
public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> {

    private static final float ASPECT_RATIO = 9f / 16f;
    private final ItemClickListener mClickListener;
    private final Context mAppContext;
    private List<MediaInfo> videos;
    private List<Integer> headerPositions = new ArrayList<Integer>();

    VideoListAdapter(ItemClickListener clickListener, Context context) {
        mClickListener = clickListener;
        mAppContext = context.getApplicationContext();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        View parent;
        if(viewType == R.layout.browse_row) {
            parent = LayoutInflater.from(context).inflate(R.layout.browse_row, viewGroup, false);
        }else {
            parent = LayoutInflater.from(context).inflate(R.layout.header_hours, viewGroup, false);
        }
        return ViewHolder.newInstance(parent);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        int type = getItemViewType(position);
        if(type == R.layout.browse_row) {
            final MediaInfo item = videos.get(position);
            MediaMetadata mm = item.getMetadata();
            viewHolder.setTitle(mm.getString(MediaMetadata.KEY_TITLE));
            viewHolder.setDescription(mm.getString(MediaMetadata.KEY_SUBTITLE));
            viewHolder.setImage(mm.getImages().get(0).getUrl().toString(), mAppContext);
            viewHolder.setHours(mm.getString(VideoProvider.TAG_HOUR), item.getStreamDuration());

            viewHolder.mMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClickListener.itemClicked(view, item, position);
                }
            });
            viewHolder.mImgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClickListener.itemClicked(view, item, position);
                }
            });

            viewHolder.mTextContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClickListener.itemClicked(view, item, position);
                }
            });
            CastSession castSession = CastContext.getSharedInstance(mAppContext).getSessionManager()
                    .getCurrentCastSession();
            viewHolder.mMenu.setVisibility(
                    (castSession != null && castSession.isConnected()) ? View.VISIBLE : View.GONE);
        } else {
            String day = getVideoDay(position+1);
            viewHolder.setTextDay(day);
        }
    }


    @Override
    public int getItemCount() {
        return videos == null ? 0 : videos.size();
    }

    public List<MediaInfo> getVideos() {
        return videos;
    }

    public int getItemViewType (int position) {
        for(int i = 0; i < headerPositions.size(); i++){
            if(headerPositions.get(i) == position)
                return R.layout.header_hours;
        }
        return R.layout.browse_row;
    }

    /**
     * A {@link RecyclerView.ViewHolder} that displays a single video in
     * the video list.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final View mParent;
        private final View mMenu;
        private final View mTextContainer;
        private TextView mTitleView;
        private TextView mDescriptionView;
        private NetworkImageView mImgView;
        private ImageLoader mImageLoader;
        private TextView mTextDay;
        private TextView mStartHour;
        private TextView mEndHour;

        static ViewHolder newInstance(View parent) {
            NetworkImageView imgView = parent.findViewById(R.id.imageView1);
            TextView titleView = parent.findViewById(R.id.textView1);
            TextView descriptionView = parent.findViewById(R.id.textView2);
            View menu = parent.findViewById(R.id.menu);
            View textContainer = parent.findViewById(R.id.text_container);
            TextView textDay = parent.findViewById(R.id.textDay);
            TextView startHour = parent.findViewById(R.id.textStartHour);
            TextView endHour = parent.findViewById(R.id.textEndHour);
            return new ViewHolder(parent, imgView, textContainer, titleView, descriptionView, menu, textDay, startHour, endHour);
        }

        private ViewHolder(View parent, NetworkImageView imgView, View textContainer, TextView titleView,
                TextView descriptionView, View menu, TextView textDay, TextView startHour, TextView endHour) {
            super(parent);
            mParent = parent;
            mImgView = imgView;
            mTextContainer = textContainer;
            mMenu = menu;
            mTitleView = titleView;
            mDescriptionView = descriptionView;
            mTextDay = textDay;
            mStartHour = startHour;
            mEndHour = endHour;
        }

        public void setTitle(String title) {
            mTitleView.setText(title);
        }

        void setDescription(String description) {
            mDescriptionView.setText(description);
        }

        void setImage(String imgUrl, Context context) {
            mImageLoader = CustomVolleyRequest.getInstance(context)
                    .getImageLoader();

            mImageLoader.get(imgUrl, ImageLoader.getImageListener(mImgView, 0, 0));
            mImgView.setImageUrl(imgUrl, mImageLoader);
        }

        public void setOnClickListener(View.OnClickListener listener) {
            mParent.setOnClickListener(listener);
        }

        ImageView getImageView() {
            return mImgView;
        }

        void setTextDay(String day) {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            String[] days = new String[]{"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
            int today = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            if(today == 0){
                today = 6;
            } else {
                today = today - 1;
            }
            mTextDay.setText(day);
            if(days[today].equals(day)) {
                mTextDay.setTextColor(Color.RED);
            } else {
                mTextDay.setTextColor(Color.rgb(100, 100, 100));
            }
        }

        void setHours(String startHour, long duration) {
            mStartHour.setText(startHour);
            mEndHour.setText(getEndHour(startHour, duration/1000));
        }

        private String getEndHour (String startHour, long duration) {
            int hour = Integer.parseInt(startHour.split(":")[0]);
            int minutes = Integer.parseInt(startHour.split(":")[1]);

            long durationMinutes = duration / 60;
            long durationHours = durationMinutes / 60;
            durationMinutes = durationMinutes % 60;

            if(minutes + durationMinutes > 59) {
                durationHours++;
                durationMinutes = minutes + durationMinutes - 60;
            }

            if(hour + durationHours > 23) {
                long diff = hour + durationHours -24;
                durationHours = diff - hour;
            }

            String endHour = (hour + durationHours) > 9 ? (hour + durationHours) + "" : "0" + (hour + durationHours);
            String endMinutes = (minutes + durationMinutes) > 9 ? (minutes + durationMinutes) + "" : "0" + (minutes + durationMinutes);
            return endHour + ":" + endMinutes;
        }
    }

    void setData(List<MediaInfo> data) {
        videos = data;
        if(data != null)
            insertHeadersBetweenHours();
        notifyDataSetChanged();
    }

    private void insertHeadersBetweenHours() {
        MediaInfo mediaInfo = VideoProvider.buildMediaInfo("", "", "", 0, "", "", "",
                "", null, "", "");
        String[] days = new String[]{"L", "M", "X", "J", "V", "S", "D"};
        int previousVideoDay = -1;
        for(int i = 0; i <videos.size() ; i++){
            int currentVideoDay = VideoProvider.findIndexOf(days, videos.get(i).getMetadata().getString(VideoProvider.TAG_DAY));
            if(currentVideoDay > previousVideoDay){
                previousVideoDay = currentVideoDay;
                videos.add(i, mediaInfo);
                headerPositions.add(i);
            }
        }
    }

    private String getVideoDay(int position) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        String[] months = new String[]{"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
        String[] daysLetter = new String[]{"L", "M", "X", "J", "V", "S", "D"};
        String[] days = new String[]{"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        String day = videos.get(position).getMetadata().getString(VideoProvider.TAG_DAY);
        int index = VideoProvider.findIndexOf(daysLetter, day);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if(dayOfWeek == 0){
            dayOfWeek = 6;
        } else {
            dayOfWeek = dayOfWeek - 1;
        }

        if(dayOfWeek != index) {
            int diff = index - dayOfWeek;
            calendar.add(Calendar.DAY_OF_MONTH, diff);
        }
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        return days[index] + ", " + dayOfMonth + " de " + months[month] + " de " + year;
    }

    /**
     * A listener called when an item is clicked in the video list.
     */
    public interface ItemClickListener {

        void itemClicked(View view, MediaInfo item, int position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
}
