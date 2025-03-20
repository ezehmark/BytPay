import MyApp from "./userDetails.tsx";
import { useEffect } from "react";
import { StatusBar } from "react-native";
import * as NavigationBar from "expo-navigation-bar";

export default function App() {
  useEffect(() => {
    const handleNavigationBar = async () => {
      await NavigationBar.setBackgroundColorAsync("#2e4a5f"); // Fixed function name
      await NavigationBar.setButtonStyleAsync("light"); // Use "light" or "dark"
    };
    handleNavigationBar();
  }, []);

  return (
    <>
      <StatusBar barStyle="dark-content" backgroundColor="#2e4a5f" />
      <MyApp />
    </>
  );
}
