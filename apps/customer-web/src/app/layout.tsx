import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Commerce | Alışveriş",
  description: "Farklı satıcıların ürünlerini keşfedin ve güvenle alışveriş yapın.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="tr">
      <body>{children}</body>
    </html>
  );
}
