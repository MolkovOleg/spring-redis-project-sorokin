import http from 'k6/http';
import {generateRandomIds, randomChoice} from "./k6_utils.js";
import {BASE_URL, CACHE_MODE} from "./config.js";


// Популярные ID (для имитации реальной нагрузки)
// ВАЖНО - у каждого виртуального пользователя свои ID
// итоговое кол-во ID = popularIds.size * VirtualUsers count
const popularIds = generateRandomIds(15, 1, 10000000);

export const options = {
    stages: [
        {duration: '5s', target: 400},
        {duration: '15s', target: 400},
        {duration: '5s', target: 0},
    ],
};

export default function () {
    // Случайных ID из популярной выборки
    const productId = randomChoice(popularIds)

    const url = `${BASE_URL}/api/v1/products/${productId}?cacheMode=${CACHE_MODE}`

    const response = http.get(url)

    if (response.status !== 200 && response.status !== 400) {
        console.log(`GET failed: ${response.status} for ID: ${productId}`);
    } else {
        console.log(`GET success: ${response.status} for ID: ${productId}`);
    }
}



