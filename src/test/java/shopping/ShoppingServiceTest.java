package shopping;

import customer.Customer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import product.Product;
import product.ProductDao;

import static org.junit.jupiter.api.Assertions.*;


class ShoppingServiceTest {
    private final ProductDao productDao = Mockito.mock(ProductDao.class);
    private final ShoppingService shoppingService = new ShoppingServiceImpl(productDao);
    private final Customer customer;

    public ShoppingServiceTest() {
        this.customer = new Customer(1L, "1");
    }

    /**
     * Нет смысла тестировать метод, так как в нем вызывается конструктор класса {@link Cart}
     */
    @Test
    void getCart() {
    }

    /**
     * Нет смысла тестировать метод, так как поведение метода определяет метод {@link ProductDao#getAll()},
     * который мы сами должны реализовать.
     */
    @Test
    void getAllProducts() {
    }

    /**
     * Нет смысла тестировать метод, так как поведение метода определяет метод {@link ProductDao#getByName(String)},
     * который мы сами должны реализовать.
     */
    @Test
    void getProductByName() {
    }

    /**
     * Тест на проверку что корзина должна очиститься после покупки (Должно ли это так работать или корзина должна очиститься)
     */
    @Test
    void buyWithCheckCartSize() throws BuyException {
        Product product = new Product("product", 2);
        Cart cart = new Cart(this.customer);
        cart.add(product, 1);

        boolean buy = this.shoppingService.buy(cart);

        assertTrue(buy);
        assertEquals(1, product.getCount());
        Mockito.verify(productDao, Mockito.times(1)).save(product);
        assertEquals(0, cart.getProducts().size());
    }

    /**
     * Тест на проверку что покупка не произойдет, так как нет нужного количества товара
     * Примечание: этот тест проходит даже если у одного {@link Customer} 2 корзины.
     */
    @Test
    void buyIfNotEnoughProductCount() throws BuyException {
        Product product = new Product("product", 5);

        Customer secondCustomer = new Customer(2L, "2");
        Cart cart1 = new Cart(this.customer);
        Cart cart2 = new Cart(secondCustomer);

        cart1.add(product, 3);
        cart2.add(product, 3);

        assertTrue(shoppingService.buy(cart1));

        BuyException exception = assertThrows(BuyException.class, () -> {
            boolean secondBuy = this.shoppingService.buy(cart1);
            assertFalse(secondBuy);
        });
        assertEquals("В наличии нет необходимого количества товара 'product'", exception.getMessage());
    }

    /**
     * Покупка пустой корзины не может произойти.
     */
    @Test
    void buyWithEmptyCart() throws BuyException {
        Cart cart = new Cart(this.customer);
        assertFalse(this.shoppingService.buy(cart));
    }

    /**
     * Покупка отрицательного количества товаров
     * Деньги спишутся у пользователя, но компания получит товар, так?
     */
    @Test
    void buyNegative() throws BuyException {
        Product product = new Product("product", 1);
        Cart cart = new Cart(this.customer);
        cart.add(product, -1);

        boolean buy = this.shoppingService.buy(cart);
        assertFalse(buy);
        Mockito.verify(productDao, Mockito.never()).save(product);
    }

    /**
     * Покупка последнее товара.
     * Пользователь может купить последндий товар, но почему-то у него не получается это сделать.
     * Всё из-за "<=" в методе Cart.validateCount()
     * Тест падает, хотя не должен.
     */
    @Test
    void buyLast() throws BuyException {
        Product product = new Product("product", 1);
        Cart cart = new Cart(this.customer);
        cart.add(product, 1);
        boolean buy = this.shoppingService.buy(cart);
        assertTrue(buy);
        Mockito.verify(productDao, Mockito.times(1)).save(product);
    }

    /**
     * Покупка в прямом смысле "ничего". Хороший бизнес может получиться.
     * При всем при этом так как сохранять нечего, то и метод {@link ProductDao#save(Product)} не должен вызываться.
     * Тест падает, хотя не должен.
     */
    @Test
    void buyZeroProductCount() throws BuyException {
        Product product = new Product("product", 1);
        Cart cart = new Cart(this.customer);
        cart.add(product, 0);

        boolean buy = this.shoppingService.buy(cart);
        assertFalse(buy);
        Mockito.verify(productDao, Mockito.never()).save(product);
    }

    /**
     * Покупка отрицательного количества товара, которого вообще нет.
     */
    @Test
    void buyNegativeCountOfZeroProduct() throws BuyException {
        Product product = new Product("product", 0);
        Cart cart = new Cart(this.customer);
        cart.add(product, -1);
        boolean buy = this.shoppingService.buy(cart);
        assertFalse(buy);
        Mockito.verify(productDao, Mockito.never()).save(product);
    }
}