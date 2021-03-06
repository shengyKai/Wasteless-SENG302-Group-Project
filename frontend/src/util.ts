export default {
    /**
     * Rate limits the input function by waiting a given amount of time calling the inner function. 
     * If interrupted with another call then this wait is reset.
     * 
     * @param func Function to rate limit
     * @param wait Time (ms) to wait before calling the function
     */
    debounce(func: (() => void), wait: number) {
        let timeout: number | undefined;
        function debounced() {
            if (timeout !== undefined) {
                clearTimeout(timeout);
            }
            timeout = setTimeout(func, wait);
        }
        return debounced;
    }
};