/*
 * Copyright (c) 2017 m2049r
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package one.mayumi.xmrwallet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.transition.MaterialContainerTransform;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import one.mayumi.xmrwallet.data.BarcodeData;
import one.mayumi.xmrwallet.data.Crypto;
import one.mayumi.xmrwallet.data.Subaddress;
import one.mayumi.xmrwallet.model.Wallet;
import one.mayumi.xmrwallet.util.Helper;
import one.mayumi.xmrwallet.util.ThemeHelper;
import one.mayumi.xmrwallet.widget.Toolbar;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import timber.log.Timber;

public class ReceiveFragment extends Fragment {

    private ProgressBar pbProgress;
    private TextView tvAddress;
    private ImageView ivQrCode;
    private ImageButton bCopyAddress;

    private Wallet wallet = null;
    private boolean isMyWallet = false;

    public interface Listener {
        void setToolbarButton(int type);

        void setTitle(String title);

        void setSubtitle(String subtitle);

        void showSubaddresses(boolean managerMode);

        Subaddress getSubaddress(int index);

        Subaddress getManuallySelectedAddress();

        Subaddress getLatestSubaddress();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_receive, container, false);

        pbProgress = view.findViewById(R.id.pbProgress);
        tvAddress = view.findViewById(R.id.tvAddress);
        ivQrCode = view.findViewById(R.id.qrCode);
        bCopyAddress = view.findViewById(R.id.bCopyAddress);


        bCopyAddress.setOnClickListener(v -> copyAddress());

        tvAddress.setOnClickListener(v -> {
            listenerCallback.showSubaddresses(false);
        });

        showProgress();
        clearQR();

        if (getActivity() instanceof GenerateReviewFragment.ListenerWithWallet) {
            wallet = ((GenerateReviewFragment.ListenerWithWallet) getActivity()).getWallet();
            show();
        } else {
            throw new IllegalStateException("no wallet info");
        }

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        final MaterialContainerTransform transform = new MaterialContainerTransform();
        transform.setDrawingViewId(R.id.fragment_container);
        transform.setDuration(getResources().getInteger(R.integer.tx_item_transition_duration));
        transform.setAllContainerColors(ThemeHelper.getThemedColor(getContext(), android.R.attr.colorBackground));
        setSharedElementEnterTransition(transform);
    }

    void copyAddress() {
        Helper.clipBoardCopy(requireActivity(), getString(R.string.label_copy_address), subaddress.getAddress());
        Toast.makeText(getActivity(), getString(R.string.message_copy_address), Toast.LENGTH_SHORT).show();
    }

    private boolean qrValid = false;

    void clearQR() {
        if (qrValid) {
            ivQrCode.setImageBitmap(null);
            qrValid = false;
        }
    }

    void setQR(Bitmap qr) {
        ivQrCode.setImageBitmap(qr);
        qrValid = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume()");
        listenerCallback.setToolbarButton(Toolbar.BUTTON_BACK);
        if (wallet != null) {
            listenerCallback.setTitle(wallet.getName());
            listenerCallback.setSubtitle(wallet.getAccountLabel());
            setNewSubaddress();
        } else {
            listenerCallback.setSubtitle(getString(R.string.status_wallet_loading));
            clearQR();
        }
    }

    private boolean isLoaded = false;

    private void show() {
        Timber.d("name=%s", wallet.getName());
        isLoaded = true;
        hideProgress();
    }

    public BarcodeData getBarcodeData() {
        if (qrValid)
            return bcData;
        else
            return null;
    }

    private BarcodeData bcData = null;

    private void generateQr() {
        Timber.d("GENQR");
        String address = subaddress.getAddress();
        if (!Wallet.isAddressValid(address)) {
            clearQR();
            Timber.d("CLEARQR");
            return;
        }
        bcData = new BarcodeData(Crypto.XMR, address, "", "");
        Bitmap qr = generate(bcData.getUriString(), 512, 512);
        if (qr != null) {
            setQR(qr);
            Timber.d("SETQR");
            Helper.hideKeyboard(getActivity());
        }
    }

    public Bitmap generate(String text, int width, int height) {
        if ((width <= 0) || (height <= 0)) return null;
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        try {
            BitMatrix bitMatrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (bitMatrix.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * height + j] = 0xffffffff;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
            bitmap = addLogo(bitmap);
            return bitmap;
        } catch (WriterException ex) {
            Timber.e(ex);
        }
        return null;
    }

    private Bitmap addLogo(Bitmap qrBitmap) {
        // addume logo & qrcode are both square
        Bitmap logo = getMoneroLogo();
        final int qrSize = qrBitmap.getWidth();
        final int logoSize = logo.getWidth();

        Bitmap logoBitmap = Bitmap.createBitmap(qrSize, qrSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(logoBitmap);
        canvas.drawBitmap(qrBitmap, 0, 0, null);
        canvas.save();
        final float sx = 0.2f * qrSize / logoSize;
        canvas.scale(sx, sx, qrSize / 2f, qrSize / 2f);
        canvas.drawBitmap(logo, (qrSize - logoSize) / 2f, (qrSize - logoSize) / 2f, null);
        canvas.restore();
        return logoBitmap;
    }

    private Bitmap logo = null;

    private Bitmap getMoneroLogo() {
        if (logo == null) {
            logo = Helper.getBitmap(getContext(), R.drawable.ic_monero_logo_b);
        }
        return logo;
    }

    public void showProgress() {
        pbProgress.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        pbProgress.setVisibility(View.GONE);
    }

    Listener listenerCallback = null;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            this.listenerCallback = (Listener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement Listener");
        }
    }

    @Override
    public void onPause() {
        Timber.d("onPause()");
        Helper.hideKeyboard(getActivity());
        super.onPause();
    }

    @Override
    public void onDetach() {
        Timber.d("onDetach()");
        if ((wallet != null) && (isMyWallet)) {
            wallet.close();
            wallet = null;
            isMyWallet = false;
        }
        super.onDetach();
    }

    private Subaddress subaddress = null;

    void setNewSubaddress() {
        final Subaddress manualAddress = listenerCallback.getManuallySelectedAddress();
        Subaddress newSubaddress = null;
        if(manualAddress != null) {
            newSubaddress = manualAddress;
        } else {
            newSubaddress = listenerCallback.getLatestSubaddress();
        }
        if (!Objects.equals(subaddress, newSubaddress)) {
            final Runnable resetSize = () -> tvAddress.animate().setDuration(125).scaleX(1).scaleY(1).start();
            tvAddress.animate().alpha(1).setDuration(125)
                    .scaleX(1.2f).scaleY(1.2f)
                    .withEndAction(resetSize).start();
        }
        subaddress = newSubaddress;
        final Context context = getContext();
        Spanned label = Html.fromHtml(context.getString(R.string.receive_subaddress,
                Integer.toHexString(ThemeHelper.getThemedColor(context, R.attr.positiveColor) & 0xFFFFFF),
                Integer.toHexString(ThemeHelper.getThemedColor(context, android.R.attr.colorBackground) & 0xFFFFFF),
                subaddress.getDisplayLabel(), subaddress.getAddress()));
        tvAddress.setText(label);
        generateQr();
    }
}
