/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.plugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;

import com.aliucord.Constants;
import com.aliucord.Utils;
import com.aliucord.entities.Plugin;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.patcher.PinePatchFn;
import com.aliucord.views.Divider;
import com.discord.databinding.WidgetChatListActionsBinding;
import com.discord.models.domain.ModelMessage;
import com.discord.simpleast.code.CodeNode;
import com.discord.simpleast.code.CodeNode$a;
import com.discord.utilities.color.ColorCompat;
import com.discord.utilities.textprocessing.Rules$createCodeBlockRule$codeStyleProviders$1;
import com.discord.utilities.textprocessing.node.BasicRenderContext;
import com.discord.utilities.textprocessing.node.BlockBackgroundNode;
import com.discord.widgets.chat.list.actions.WidgetChatListActions;
import com.lytefast.flexinput.R$b;
import com.lytefast.flexinput.R$h;

@SuppressWarnings({"unchecked", "unused"})
public class ViewRaw extends Plugin {
    public ViewRaw() {
        needsResources = true;
    }

    public static class Page extends SettingsPage {
        public ModelMessage message;

        @Override
        @SuppressWarnings("ResultOfMethodCallIgnored")
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            setActionBarTitle("Raw message by " + message.getAuthor().r());
            setActionBarSubtitle("View Raw");
        }

        @Override
        @SuppressLint("SetTextI18n")
        public void onViewBound(View view) {
            super.onViewBound(view);

            var context = view.getContext();
            var layout = (LinearLayout) ((NestedScrollView) ((CoordinatorLayout) view).getChildAt(1)).getChildAt(0);
            var padding = Utils.getDefaultPadding();

            var content = message.getContent();
            if (content != null && !content.equals("")) {
                var textView = new TextView(context);
                var node = new BlockBackgroundNode<>(false, new CodeNode<BasicRenderContext>(
                        new CodeNode$a.b<>(content), "", Rules$createCodeBlockRule$codeStyleProviders$1.INSTANCE
                ));
                var builder = new SpannableStringBuilder();
                node.render(builder, new RenderContext(context));
                textView.setText(builder);
                textView.setTextIsSelectable(true);
                textView.setPadding(padding, padding, padding, padding);
                layout.addView(textView);
                layout.addView(new Divider(context));
            }

            var header = new TextView(context, null, 0, R$h.UiKit_Settings_Item_Header);
            header.setText("All Raw Data");
            header.setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold));
            layout.addView(header);

            var textView = new TextView(context);
            var node = new BlockBackgroundNode<>(false, new CodeNode<BasicRenderContext>(
                    new CodeNode$a.b<>(Utils.toJsonPretty(message)), "json", Rules$createCodeBlockRule$codeStyleProviders$1.INSTANCE
            ));
            var builder = new SpannableStringBuilder();
            node.render(builder, new RenderContext(context));
            textView.setText(builder);
            textView.setTextIsSelectable(true);
            textView.setPadding(padding, 0, padding, padding);
            layout.addView(textView);
        }
    }

    public static class RenderContext implements BasicRenderContext {
        private final Context context;
        public RenderContext(Context ctx) {
            context = ctx;
        }

        @Override
        public Context getContext() {
            return context;
        }
    }

    @NonNull
    @Override
    public Manifest getManifest() {
        var manifest = new Manifest();
        manifest.authors = new Manifest.Author[]{ new Manifest.Author("Juby210", 324622488644616195L) };
        manifest.description = "View & Copy raw message and markdown.";
        manifest.version = "1.0.2";
        manifest.updateUrl = "https://raw.githubusercontent.com/Juby210/Aliucord-plugins/builds/updater.json";
        return manifest;
    }

    @Override
    @SuppressLint("SetTextI18n")
    public void start(Context ctx) throws Throwable {
        var icon = ResourcesCompat.getDrawable(resources,
                resources.getIdentifier("ic_viewraw", "drawable", "com.aliucord.plugins"), null);
        var id = View.generateViewId();

        var c = WidgetChatListActions.class;
        var getBinding = c.getDeclaredMethod("getBinding");
        getBinding.setAccessible(true);

        patcher.patch(c, "configureUI", new Class<?>[]{ WidgetChatListActions.Model.class }, new PinePatchFn(callFrame -> {
            try {
                var binding = (WidgetChatListActionsBinding) getBinding.invoke(callFrame.thisObject);
                if (binding == null) return;
                TextView viewRaw = binding.a.findViewById(id);
                var viewRawPage = new Page();
                viewRawPage.message = ((WidgetChatListActions.Model) callFrame.args[0]).getMessage();
                viewRaw.setOnClickListener(e -> Utils.openPageWithProxy(e.getContext(), viewRawPage));
            } catch (Throwable ignored) {}
        }));

        patcher.patch(c, "onViewCreated", new Class<?>[]{ View.class, Bundle.class }, new PinePatchFn(callFrame -> {
            var linearLayout = (LinearLayout) ((NestedScrollView) callFrame.args[0]).getChildAt(0);
            var context = linearLayout.getContext();
            var viewRaw = new TextView(context, null, 0, R$h.UiKit_Settings_Item_Icon);
            viewRaw.setText("View Raw");
            if (icon != null) icon.setTint(ColorCompat.getThemedColor(context, R$b.colorInteractiveNormal));
            viewRaw.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
            viewRaw.setId(id);
            linearLayout.addView(viewRaw);
        }));
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}