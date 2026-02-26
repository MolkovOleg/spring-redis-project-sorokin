/**
 * Общие утильные функции для k6 тестирования
 * Можно использовать в любых проектах
 */

/**
 * Генерирует случайное число в диапазоне [min, max]
 */
export function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

/**
 * Генерирует случайное число с плавающей точкой в диапазоне [min, max]
 */
export function randomFloat(min, max, decimals = 2) {
    const value = Math.random() * (max - min) + min;
    return Math.round(value * Math.pow(10, decimals)) / Math.pow(10, decimals);
}

/**
 * Генерирует случайную строку заданной длины
 */
export function randomString(length = 8) {
    return Math.random().toString(36).substring(2, 2 + length);
}

/**
 * Выбирает случайный элемент из массива
 */
export function randomChoice(array) {
    return array[Math.floor(Math.random() * array.length)];
}

/**
 * Генерирует массив случайных ID из диапазона
 */
export function generateRandomIds(count, minId, maxId) {
    const ids = [];
    for (let i = 0; i < count; i++) {
        ids.push(randomInt(minId, maxId));
    }
    return ids;
}