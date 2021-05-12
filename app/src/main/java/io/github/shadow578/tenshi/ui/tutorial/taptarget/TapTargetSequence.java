package io.github.shadow578.tenshi.ui.tutorial.taptarget;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.shadow578.tenshi.extensionslib.lang.BiConsumer;
import io.github.shadow578.tenshi.extensionslib.lang.Consumer;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.with;

/**
 * a sequence of {@link MaterialTapTargetPrompt}
 */
public class TapTargetSequence {

    /**
     * internal list of sequence items
     */
    @NonNull
    private final ArrayList<Item> items = new ArrayList<>();

    /**
     * listener for sequence step
     */
    @Nullable
    private BiConsumer<Item, Item> stepListener = null;

    /**
     * listener for sequence end
     */
    @Nullable
    private Consumer<Boolean> endListener = null;

    /**
     * add multiple prompts to the sequence.
     * prompts cannot set a {@link MaterialTapTargetPrompt.PromptStateChangeListener}
     *
     * @param itms the items to add
     * @return instance
     */
    @NonNull
    public TapTargetSequence add(@NonNull MaterialTapTargetPrompt.Builder... itms) {
        for (MaterialTapTargetPrompt.Builder itm : itms)
            add(itm);
        return this;
    }

    /**
     * add a prompt to the sequence.
     * prompts cannot set a {@link MaterialTapTargetPrompt.PromptStateChangeListener}
     *
     * @param itm the item to add
     * @return instance
     */
    @NonNull
    public TapTargetSequence add(@NonNull MaterialTapTargetPrompt.Builder itm) {
        return add(itm, 0);
    }

    /**
     * add a prompt to the sequence.
     * prompts cannot set a {@link MaterialTapTargetPrompt.PromptStateChangeListener}
     *
     * @param itm the item to add
     * @param id  the id of the item
     * @return instance
     */
    @NonNull
    public TapTargetSequence add(@NonNull MaterialTapTargetPrompt.Builder itm, int id) {
        items.add(new Item(itm, id));
        return this;
    }

    /**
     * set the listener for sequence step
     *
     * @param listener the listener to set. p1 is current item, p2 is upcoming item
     * @return instance
     */
    @NonNull
    public TapTargetSequence onStep(@Nullable BiConsumer<Item, Item> listener) {
        stepListener = listener;
        return this;
    }

    /**
     * set the listener for sequence end
     *
     * @param listener the listener to set. p1 is dismissed flag
     * @return instance
     */
    @NonNull
    public TapTargetSequence onEnd(@Nullable Consumer<Boolean> listener) {
        endListener = listener;
        return this;
    }

    /**
     * start the sequence
     */
    public void start() {
        // abort if no items
        if (items.size() <= 0)
            throw new IllegalStateException("cannot start a sequence with 0 elements!");

        // setup the element listeners
        for (int i = 0; i < items.size(); i++) {
            // get next item, if any
            final Item current = items.get(i);
            final Item next = (i + 1 < items.size()) ? items.get(i + 1) : null;

            // set listener
            current.promptBuilder.setPromptStateChangeListener((prompt, state)
                    -> promptChangeListenerImpl(state, current, next));
        }

        // show the first item
        showItem(items.get(0));
    }

    /**
     * internal prompt state change listener.
     * handles showing the next prompt
     *
     * @param state      the state the prompt changed to
     * @param currentItm the current item
     * @param nextItm    the next item to show
     */
    private void promptChangeListenerImpl(int state,
                                          @NonNull Item currentItm,
                                          @Nullable Item nextItm) {
        switch (state) {
            case MaterialTapTargetPrompt.STATE_FINISHED:
                // prompt finished without being dismissed, continue to the next if any
                if (notNull(nextItm)) {
                    // show next
                    invokeListenerStep(currentItm, nextItm);
                    showItem(nextItm);
                } else {
                    // end of sequence
                    invokeListenerEnd(false);
                }
                break;
            case MaterialTapTargetPrompt.STATE_DISMISSED:
                // prompt was dismissed, cancel sequence
                invokeListenerEnd(true);
                break;
            case MaterialTapTargetPrompt.STATE_NOT_SHOWN:
            case MaterialTapTargetPrompt.STATE_REVEALING:
            case MaterialTapTargetPrompt.STATE_REVEALED:
            case MaterialTapTargetPrompt.STATE_FOCAL_PRESSED:
            case MaterialTapTargetPrompt.STATE_DISMISSING:
            case MaterialTapTargetPrompt.STATE_FINISHING:
            case MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED:
            case MaterialTapTargetPrompt.STATE_SHOW_FOR_TIMEOUT:
            case MaterialTapTargetPrompt.STATE_BACK_BUTTON_PRESSED:
            default:
                // invalid state or dont care
                break;
        }
    }

    /**
     * show a item
     *
     * @param itm the item to show
     */
    private void showItem(@NonNull Item itm) {
        itm.promptBuilder.show();
    }

    /**
     * invoke {@link #stepListener}
     *
     * @param current the current item
     * @param next    the next item
     */
    private void invokeListenerStep(@NonNull Item current, @NonNull Item next) {
        with(stepListener, l -> l.invoke(current, next));
    }

    /**
     * invoke {@link #endListener}
     *
     * @param dismissed was the sequence dismissed?
     */
    private void invokeListenerEnd(boolean dismissed) {
        with(endListener, l -> l.invoke(dismissed));
    }

    /**
     * a item in a sequence
     */
    public static final class Item {
        /**
         * prompt builder for this item
         */
        final MaterialTapTargetPrompt.Builder promptBuilder;

        /**
         * id of this item
         */
        public final int id;

        public Item(MaterialTapTargetPrompt.Builder promptBuilder, int id) {
            this.promptBuilder = promptBuilder;
            this.id = id;
        }
    }
}
