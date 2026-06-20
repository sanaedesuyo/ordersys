import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])

  const total = computed(() =>
    items.value.reduce((sum, item) => sum + item.price * item.quantity, 0),
  )

  const count = computed(() =>
    items.value.reduce((sum, item) => sum + item.quantity, 0),
  )

  function addItem(dish) {
    const existing = items.value.find((i) => i.dishId === dish.id)
    if (existing) {
      existing.quantity++
    } else {
      items.value.push({
        dishId: dish.id,
        dishName: dish.name,
        price: dish.price,
        quantity: 1,
        size: 'LARGE',
        extras: [],
        note: '',
      })
    }
  }

  function removeItem(dishId) {
    const idx = items.value.findIndex((i) => i.dishId === dishId)
    if (idx !== -1) {
      if (items.value[idx].quantity > 1) {
        items.value[idx].quantity--
      } else {
        items.value.splice(idx, 1)
      }
    }
  }

  function clearCart() {
    items.value = []
  }

  return { items, total, count, addItem, removeItem, clearCart }
})
