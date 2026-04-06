package myst.synthetic.item;

import myst.synthetic.MystcraftItems;
import myst.synthetic.block.entity.BlockEntitySlantBoard;
import myst.synthetic.component.FolderDataComponent;
import myst.synthetic.component.MystcraftDataComponents;
import myst.synthetic.component.PortfolioDataComponent;
import myst.synthetic.menu.PortfolioMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ItemPortfolio extends Item {

    public ItemPortfolio(Properties props) {
        super(props);
    }

    public static PortfolioDataComponent getData(ItemStack stack) {
        return stack.getOrDefault(
                MystcraftDataComponents.PORTFOLIO_DATA,
                PortfolioDataComponent.EMPTY
        );
    }

    public static void setData(ItemStack stack, PortfolioDataComponent data) {
        stack.set(MystcraftDataComponents.PORTFOLIO_DATA, data);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        ItemStack portfolio = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            ItemStack offhand = player.getOffhandItem();

            if (offhand.is(MystcraftItems.PAGE)) {
                absorbPage(player, portfolio, offhand);
                return InteractionResult.CONSUME;
            }

            if (offhand.is(MystcraftItems.FOLDER)) {
                absorbFolder(player, portfolio, offhand);
                return InteractionResult.CONSUME;
            }

            int hostSlot = player.getInventory().getSelectedSlot();

            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, ignored) -> new PortfolioMenu(containerId, playerInventory, hostSlot),
                    portfolio.getHoverName()
            ));
        }

        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();

        if (player == null) {
            return InteractionResult.PASS;
        }

        Level level = ctx.getLevel();

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack portfolio = ctx.getItemInHand();

        if (level.getBlockEntity(ctx.getClickedPos()) instanceof BlockEntitySlantBoard board) {
            ItemStack page = board.getPage();

            if (!page.isEmpty() && page.is(MystcraftItems.PAGE)) {
                absorbPage(player, portfolio, page);
                board.clearPage();
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    private static void absorbPage(Player player, ItemStack portfolio, ItemStack page) {
        PortfolioDataComponent data = getData(portfolio);
        data = data.add(page);
        setData(portfolio, data);

        page.shrink(1);

        player.playSound(SoundEvents.ITEM_PICKUP, 0.2f, 1.0f);
    }

    private static void absorbFolder(Player player, ItemStack portfolio, ItemStack folder) {
        PortfolioDataComponent portfolioData = getData(portfolio);
        FolderDataComponent folderData = ItemFolder.getFolderData(folder);

        boolean changed = false;

        for (ItemStack stack : folderData.toSlotList()) {
            if (!stack.isEmpty() && stack.is(MystcraftItems.PAGE)) {
                portfolioData = portfolioData.add(stack);
                changed = true;
            }
        }

        if (!changed) {
            return;
        }

        setData(portfolio, portfolioData);
        ItemFolder.saveInventory(
                folder,
                NonNullList.withSize(FolderDataComponent.MAX_SLOTS, ItemStack.EMPTY)
        );

        player.playSound(SoundEvents.ITEM_PICKUP, 0.2f, 1.0f);
    }
}