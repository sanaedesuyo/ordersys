export const DISH_TYPES = [
  { value: 'MAIN_DISH', label: '主食', icon: '🍚' },
  { value: 'BEVERAGE', label: '饮料', icon: '🥤' },
  { value: 'DESSERT', label: '甜点', icon: '🍰' },
  { value: 'SNACK', label: '小吃', icon: '🍟' },
  { value: 'SIDE_DISH', label: '配菜', icon: '🥗' },
  { value: 'SOUP', label: '汤品', icon: '🍲' },
]

export const DISH_FILTERS = [
  { value: 'ALL', label: '全部' },
  ...DISH_TYPES.map((t) => ({ value: t.value, label: `${t.icon} ${t.label}` })),
]

export function typeLabel(type) {
  return DISH_TYPES.find((t) => t.value === type)?.label ?? type
}

export function typeIcon(type) {
  return DISH_TYPES.find((t) => t.value === type)?.icon ?? '🍽️'
}
