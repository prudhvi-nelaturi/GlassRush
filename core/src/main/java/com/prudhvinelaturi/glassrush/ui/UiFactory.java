package com.prudhvinelaturi.glassrush.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Builds a minimal runtime {@link Skin} from solid-color pixmaps so screens can use
 * Scene2D buttons/labels without any external skin atlas or asset files — consistent
 * with the game's procedural-art approach (no sprite dependencies for v1 UI).
 */
public final class UiFactory {
    private UiFactory() {}

    public static Skin createSkin(BitmapFont font) {
        Skin skin = new Skin();
        skin.add("default-font", font);

        skin.add("button-up", solidTexture(new Color(0.16f, 0.18f, 0.24f, 1f)));
        skin.add("button-down", solidTexture(new Color(0f, 0.85f, 1f, 1f)));
        skin.add("button-disabled", solidTexture(new Color(0.1f, 0.1f, 0.12f, 1f)));

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = new TextureRegionDrawable(skin.getRegion("button-up"));
        buttonStyle.down = new TextureRegionDrawable(skin.getRegion("button-down"));
        buttonStyle.disabled = new TextureRegionDrawable(skin.getRegion("button-disabled"));
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.downFontColor = new Color(0.05f, 0.05f, 0.08f, 1f);
        skin.add("default", buttonStyle);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        return skin;
    }

    private static com.badlogic.gdx.graphics.g2d.TextureRegion solidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new com.badlogic.gdx.graphics.g2d.TextureRegion(texture);
    }
}
