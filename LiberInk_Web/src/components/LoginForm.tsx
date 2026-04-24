import React, { useState } from 'react';
import { auth } from '../firebase'; // Імпортуємо наш конфіг
import { signInWithEmailAndPassword } from 'firebase/auth';

const LoginForm = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      // Реальний вхід через Firebase
      const userCredential = await signInWithEmailAndPassword(auth, email, password);
      console.log("Успішно увійшли:", userCredential.user);
      alert("Ви успішно увійшли в LiberInk!");
    } catch (err: any) {
      console.error(err);
      setError("Неправильний email або пароль");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-liber-cream p-4">
      <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl p-8 border border-gray-100">
        
        {/* Заголовок у твоєму стилі */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-serif font-bold text-gray-800 mb-2">LiberInk</h1>
          <p className="text-gray-500 italic">Твій простір для творчості</p>
        </div>

        <form onSubmit={handleLogin} className="space-y-6">
          {/* Поле Email */}
          <div className="flex flex-col gap-2">
            <label className="text-sm font-semibold text-gray-700 ml-1">Email</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="writer@liberink.com"
              className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-liber-orange focus:ring-2 focus:ring-liber-orange/20 outline-none transition-all"
            />
          </div>

          {/* Поле Пароля */}
          <div className="flex flex-col gap-2">
            <label className="text-sm font-semibold text-gray-700 ml-1">Пароль</label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:border-liber-orange focus:ring-2 focus:ring-liber-orange/20 outline-none transition-all"
            />
          </div>

          {/* Кнопка входу */}
          <button
            type="submit"
            disabled={isLoading}
            className="w-full py-4 bg-liber-orange hover:bg-orange-500 text-white font-bold rounded-xl shadow-lg shadow-orange-200 transition-all active:scale-[0.98] disabled:opacity-70"
          >
            {isLoading ? "Завантаження..." : "Увійти до системи"}
          </button>
        </form>

        {/* Додаткові посилання */}
        <div className="mt-8 text-center text-sm text-gray-400">
          <p>Ще не маєте акаунту? <span className="text-liber-orange font-semibold cursor-pointer hover:underline">Створити</span></p>
        </div>
      </div>
    </div>
  );
};

export default LoginForm;