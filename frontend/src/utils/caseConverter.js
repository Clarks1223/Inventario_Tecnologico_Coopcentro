const isPlainObject = (value) =>
  Object.prototype.toString.call(value) === '[object Object]';

const snakeToCamelKey = (key) => key.replace(/_([a-z0-9])/g, (_, c) => c.toUpperCase());
const camelToSnakeKey = (key) => key.replace(/[A-Z]/g, (c) => `_${c.toLowerCase()}`);

const transform = (value, keyFn) => {
  if (Array.isArray(value)) return value.map((item) => transform(item, keyFn));
  if (isPlainObject(value)) {
    return Object.entries(value).reduce((acc, [key, val]) => {
      acc[keyFn(key)] = transform(val, keyFn);
      return acc;
    }, {});
  }
  return value;
};

export const keysToCamel = (value) => transform(value, snakeToCamelKey);
export const keysToSnake = (value) => transform(value, camelToSnakeKey);

const trimValue = (value) => {
  if (Array.isArray(value)) return value.map(trimValue);
  if (isPlainObject(value)) {
    return Object.entries(value).reduce((acc, [key, val]) => {
      acc[key] = trimValue(val);
      return acc;
    }, {});
  }
  if (typeof value === 'string') return value.trim();
  return value;
};

export const trimStrings = (value) => trimValue(value);
