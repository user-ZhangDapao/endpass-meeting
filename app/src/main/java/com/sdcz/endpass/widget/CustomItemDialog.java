package com.sdcz.endpass.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sdcz.endpass.R;


public class CustomItemDialog extends Dialog {

    public CustomItemDialog(Context context) {
        super(context);
    }

    public CustomItemDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private Context context;
        private String title;
        private String message;
        private String message2;
        private String message3;
        private String message6;
        private String positiveButtonText;
        private String negativeButtonText;
        private View contentView;
        private OnClickListener positiveButtonClickListener;
        private OnClickListener negativeButtonClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        public void setMessage(String message) {
            this.message = message;
        }
        public void setMessage2(String message2) {
            this.message2 = message2;
        }
        public void setMessage3(String message3) {
            this.message3 = message3;
        }

        public void setMessage6(String message6) {
            this.message6 = message6;
        }

        /**
         * Set the Dialog message from resource
         *
         * @param
         * @return
         */
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        /**
         * Set the Dialog title from resource
         *
         * @param title
         * @return
         */
        public void setTitle(int title) {
            this.title = (String) context.getText(title);
        }

        /**
         * Set the Dialog title from String
         *
         * @param title
         * @return
         */

        public void setTitle(String title) {
            this.title = title;
        }

        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }

        /**
         * Set the positive button resource and it's listener
         *
         * @param positiveButtonText
         * @return
         */
        public Builder setPositiveButton(int positiveButtonText,
                                         OnClickListener listener) {
            this.positiveButtonText = (String) context
                    .getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }

        public void setPositiveButton(String positiveButtonText,
                                         OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
        }

        public Builder setNegativeButton(int negativeButtonText,
                                         OnClickListener listener) {
            this.negativeButtonText = (String) context
                    .getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }

        public void setNegativeButton(String negativeButtonText,
                                         OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
        }

        public CustomItemDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final CustomItemDialog dialog = new CustomItemDialog(context, R.style.Dialog);
            View layout = inflater.inflate(R.layout.dialog_item, null);
            dialog.addContentView(layout, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            // set the dialog title
            ((TextView) layout.findViewById(R.id.title)).setText(title);
            // set the confirm button
            if (positiveButtonText != null) {
                ((TextView) layout.findViewById(R.id.positiveTextView))
                        .setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    ((TextView) layout.findViewById(R.id.positiveTextView))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    positiveButtonClickListener.onClick(dialog,
                                            DialogInterface.BUTTON_POSITIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.positiveTextView).setVisibility(
                        View.GONE);
            }
            // set the cancel button
            if (negativeButtonText != null) {
                ((TextView) layout.findViewById(R.id.negativeTextView))
                        .setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    ((TextView) layout.findViewById(R.id.negativeTextView))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    negativeButtonClickListener.onClick(dialog,
                                            DialogInterface.BUTTON_NEGATIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.negativeTextView).setVisibility(
                        View.GONE);
            }
            // set the content message
            if (!"".equals(message)&&message != null) {
                ((TextView) layout.findViewById(R.id.message)).setText(message);
            }else {
                ((TextView) layout.findViewById(R.id.message)).setVisibility(View.GONE);
            }
            if (!"".equals(message2)&&message2 != null) {
                ((TextView) layout.findViewById(R.id.message2)).setText(message2);
            }else {
                ((TextView) layout.findViewById(R.id.message2)).setVisibility(View.GONE);
            }
            if (!"".equals(message3)&&message3 != null) {
                ((TextView) layout.findViewById(R.id.message3)).setText(message3);
            }else {
                ((TextView) layout.findViewById(R.id.message3)).setVisibility(View.GONE);
            }
            if (!"".equals(message6)&&message6 != null) {
                ((TextView) layout.findViewById(R.id.message6)).setText(message6);
            }else {
                ((TextView) layout.findViewById(R.id.message6)).setVisibility(View.GONE);
            }
            if (contentView != null) {
                // if no message set
                // add the contentView to the dialog body
                ((LinearLayout) layout.findViewById(R.id.content))
                        .removeAllViews();
                ((LinearLayout) layout.findViewById(R.id.content))
                        .addView(contentView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            }
            dialog.setContentView(layout);
            return dialog;
        }
    }
}
