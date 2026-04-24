import { initializeApp } from "firebase/app";
import { getAuth, GoogleAuthProvider } from "firebase/auth";
import { getFirestore } from "firebase/firestore"; // 1. Додай цей імпорт

const firebaseConfig = {
  apiKey: "AIzaSyDG0HvJnb8JPhOim-boPHdWXL8o3nZQ_ug",
  authDomain: "liberink-1.firebaseapp.com",
  projectId: "liberink-1",
  storageBucket: "liberink-1.firebasestorage.app",
  messagingSenderId: "74885674202",
  appId: "1:74885674202:web:1ecb783ec0b3b929e0a1e3",
  measurementId: "G-M3538ZDX5D"
};

// Ініціалізація додатка
const app = initializeApp(firebaseConfig);

// Експортуємо інструменти
export const auth = getAuth(app);
export const googleProvider = new GoogleAuthProvider();
export const db = getFirestore(app); // 2. Додай цей експорт