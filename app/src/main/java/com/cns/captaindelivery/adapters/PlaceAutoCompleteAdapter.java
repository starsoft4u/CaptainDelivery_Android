package com.cns.captaindelivery.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cns.captaindelivery.GlobalConst;
import com.cns.captaindelivery.R;
import com.cns.captaindelivery.activities.CustomerMainActivity;
import com.cns.captaindelivery.activities.CustomerShopInfoActivity;
import com.cns.captaindelivery.fragments.CustomerOrderFragment;
import com.cns.captaindelivery.models.InfoGooglePlace;
import com.seatgeek.placesautocomplete.PlacesApi;
import com.seatgeek.placesautocomplete.adapter.AbstractPlacesAutocompleteAdapter;
import com.seatgeek.placesautocomplete.history.AutocompleteHistoryManager;
import com.seatgeek.placesautocomplete.model.AutocompleteResultType;
import com.seatgeek.placesautocomplete.model.Place;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PlaceAutoCompleteAdapter extends AbstractPlacesAutocompleteAdapter {

    public PlaceAutoCompleteAdapter(final Context context, final PlacesApi api, final AutocompleteResultType resultType, final AutocompleteHistoryManager history) {
        super(context, api, resultType, history);
    }

    @Override
    protected View newView(final ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place_autocomplete, parent, false);
    }

    @Override
    protected void bindView(final View view, final Place item) {
        ((TextView) view).setText(item.description);
    }
}
