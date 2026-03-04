package com.am24.weatherforecastapp.utils

/**
 * Утиліта для транслітерації українського тексту на латиницю.
 * Допомагає API знайти місто, якщо воно не розпізнає назву кирилицею.
 */
object TransliterationUtils {

    /**
     * Перетворює українські літери на їх латинські відповідники.
     * @param text вихідна назва міста
     * @return текст латиницею
     */
    fun transliterate(text: String): String {
        val map = mapOf(
            'а' to "a", 'б' to "b", 'в' to "v", 'г' to "h", 'ґ' to "g",
            'д' to "d", 'е' to "e", 'є' to "ye", 'ж' to "zh", 'з' to "z",
            'и' to "y", 'і' to "i", 'ї' to "i", 'й' to "y", 'к' to "k",
            'л' to "l", 'м' to "m", 'н' to "n", 'о' to "o", 'п' to "p",
            'р' to "r", 'с' to "s", 'т' to "t", 'у' to "u", 'ф' to "f",
            'х' to "kh", 'ц' to "ts", 'ч' to "ch", 'ш' to "sh", 'щ' to "shch",
            'ю' to "yu", 'я' to "ya", 'ь' to "", '’' to "", '\'' to "",

            'А' to "A", 'Б' to "B", 'В' to "V", 'Г' to "H", 'Ґ' to "G",
            'Д' to "D", 'Е' to "E", 'Є' to "Ye", 'Ж' to "Zh", 'З' to "Z",
            'И' to "Y", 'І' to "I", 'Ї' to "Yi", 'Й' to "Y", 'К' to "K",
            'Л' to "L", 'М' to "M", 'Н' to "N", 'О' to "O", 'П' to "P",
            'Р' to "R", 'С' to "S", 'Т' to "T", 'У' to "U", 'Ф' to "F",
            'Х' to "Kh", 'Ц' to "Ts", 'Ч' to "Ch", 'Ш' to "Sh", 'Щ' to "Shch",
            'Ю' to "Yu", 'Я' to "Ya", 'Ь' to ""
        )

        /**
         * Проходимо по кожному символу вхідного тексту:
         * 1. Шукаємо символ у нашій мапі (map[char]).
         * 2. Якщо знайшли — беремо латинську версію.
         * 3. Якщо не знайшли (наприклад, це цифра чи пробіл) — залишаємо як є.
         * 4. Об'єднуємо все назад у рядок (joinToString).
         */
        val transliterated = text.map { char -> map[char] ?: char.toString() }.joinToString("")

        return transliterated
    }
}