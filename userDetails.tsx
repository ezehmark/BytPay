import React, { useState, useRef, useEffect } from "react";
import {
  View,
  Text,
  Button,
  TouchableOpacity,
  TextInput,
  ActivityIndicator,
  FlatList,
  ScrollView,
  Alert,
  Platform,
  StyleSheet,
} from "react-native";
import { KeyboardAwareScrollView } from "react-native-keyboard-aware-scroll-view";
import axios from "axios";
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  useAnimatedGestureHandler,
  withTiming,
  withSequence,
  useAnimatedProps,
  withRepeat,
  Easing,
  runOnJS,
  cancelAnimation,
} from "react-native-reanimated";
import {
  PanGestureHandler,
  GestureHandlerRootView,
  State,
} from "react-native-gesture-handler";
import { LinearGradient } from "expo-linear-gradient";
import { BlurView } from "expo-blur";
import NetInfo from "@react-native-community/netinfo";
const MyApp: React.FC = () => {
  const [userDetails, setUserDetails] = useState("");
  const [loading, setLoading] = useState(false);
  const [visible, setVisible] = useState(false);
  const [feedback, setFeedback] = useState(null);

  const [isUserDetails, setIsUserDetails] = useState(false);
  const userDetailsChecker = () => {
    if (userDetails && userDetails.length > 0) {
      setIsUserDetails(true);
    }
  };

  const [backendActive, setBackendActive] = useState(true);

  const notifyBoxTop = useSharedValue(-40);
  const notifyBoxOpacity = useSharedValue(1);
  const notifyBoxAnim = useAnimatedStyle(() => {
    return { top: notifyBoxTop.value, opacity: notifyBoxOpacity.value };
  });

  const [notifyMsg, setNotifyMsg] = useState("");

  const dropDownChanger = () => {
    setBackendActive(true);
    notifyBoxTop.value = withSequence(
      withTiming(40, { duration: 500 }),
      withTiming(40, { duration: 1200 }),
      withTiming(-40, { duration: 700 }),
    );
    notifyBoxOpacity.value = withSequence(
      withTiming(1, { duration: 500 }),
      withTiming(1, { duration: 1000 }),
      withTiming(0, { duration: 800 }),
    );
  };

  const checkHealth = async () => {
    try {
      const response = await axios.get("/health");

      if (response.status === 200) {
        dropDownChanger();
        setNotifyMsg("Backend is Deployed and Awake!");
      } else {
        setBackendActive(false);
      }
    } catch (error) {
      dropDownChanger();
      setNotifyMsg(`There is a backend failure: ${error.message}`);
    } finally {
      setBackendActive(false);
      console.log("Backend Checks Done ✅");
    }
  };
  useEffect(() => {
    checkHealth();
  }, []);
  const [connected, setConnected] = useState(false);

  const handleConnection = () => {
    NetInfo.addEventListener((state) => {
      setConnected(state.isConnected);
    });
  };
  useEffect(() => {
    handleConnection();
    setInterval(() => {
      handleConnection();
    }, 1000);
  }, []);

  const [uploaded, setUploaded] = useState(false);
  const [input, setInput] = useState("");
  const emailRef = useRef();
  const [progress, setProgress] = useState(0);

  const [borderCheck, setBorderCheck] = useState(false);

  const [people, setPeople] = useState([]);

  const myPeople = people.map((peopleItem, index) => ({
    id: index.toString(),
    ...peopleItem,
  }));
  const [name, setName] = useState("");

  const [focused, setFocused] = useState(false);

  const myRight = useSharedValue(-100);
  const myWidth = useSharedValue(100);
  const appearAnim = useAnimatedStyle(() => {
    return { right: `${myRight.value}%`, width: myWidth.value };
  });
  const myLeft = useSharedValue(-100);

  const appearAnim1 = useAnimatedStyle(() => {
    return { left: `${myLeft.value}%`, width: myWidth.value };
  });

  useEffect(() => {
    setTimeout(() => {
      myLeft.value = withSequence(
        withTiming(26, { duration: 1200, easing: Easing.inOut(Easing.ease) }),
        withTiming(0, { duration: 400, easing: Easing.inOut(Easing.ease) }),
        withTiming(10, { duration: 800, easing: Easing.in(Easing.ease) }),
      );

      myRight.value = withSequence(
        withTiming(26, { duration: 1200, easing: Easing.inOut(Easing.ease) }),
        withTiming(0, { duration: 400, easing: Easing.inOut(Easing.ease) }),
        withTiming(10, { duration: 800, easing: Easing.in(Easing.ease) }),
      );

      myWidth.value = withSequence(
        withTiming(100, { duration: 1000, easing: Easing.inOut(Easing.ease) }),
        withTiming(80, { duration: 200, easing: Easing.inOut(Easing.ease) }),
        withTiming(110, { duration: 1150, easing: Easing.in(Easing.ease) }),
      );
    }, 1000);
  }, []);

  useEffect(() => {
    if (userDetails && userDetails.length > 1) {
      setIsUserDetails(true);
    } else {
      setIsUserDetails(false);
    }
  }, [userDetails]);

  const handleFetch = async () => {
    setLoading(true);
    setFetch(true);

    await axios
      .get(`https://mybackend-oftz.onrender.com/api/userDetails/${name}?`)
      .then((response) => {
        setUserDetails(
          `${response.data.name} is now loaded from the best backend. He is ${response.data.age} years old.`,
        );
      })
      .catch((error) => {
        if (error.response && error.response.status === 404) {
          setUserDetails(error.response.data.message);
        }
      })
      .finally(() => {
        setTimeout(() => {
          setLoading(false);
          setFetch(false);
        }, 2000);
        setVisible(true);
      });
  };

  const searchClick=()=>{
  handleFetch()}

  const [selected, setSelected] = useState<string[]>([]);
  const isDragging = useSharedValue(false);

  const scrollDistance = useSharedValue(0);
  const ListAnim = useAnimatedStyle(() => {
    return { transform: [{ translateX: scrollDistance.value }] };
  });
  const triggerScroll = () => {
    scrollDistance.value = withRepeat(
      withTiming(-2500, { duration: 15000, easing: Easing.linear }),-1,false)
  };

  useEffect(() => {
    if (myPeople.length > 0 || selected) {
      setTimeout(() => {
       runOnJS(triggerScroll)();
      }, 4000);
    }
  }, [myPeople,selected]);

  const handlePost = async () => {
    setLoading(true);
    setUpload(true);

    await axios
      .post(
        "https://mybackend-oftz.onrender.com/api/userDetails",
        {
          newPeople: [
            { name: "mummy", age: "60" },
            { name: "David", age: "45" },
            { name: "Joshua", age: "25" },
          ],
        },
        { headers: { "Content-Type": "application/json" } },
      )
      .then((response) => {
        setPeople(response.data);
      })
      .catch((error) => {
        console.error(error);
      })
      .finally(() => {
        setTimeout(() => {
          setLoading(false);
          setUpload(false);
        }, 2000);
        setVisible(true);
        userDetailsChecker();
        setUploaded(true);
      });
  };
  const email = [
    "ezehmaker@gmail.com",
    "simplebiggly@gmail.com",
    "powerdonew@gmail.com",
    "younggreat303@gmail.com",
    "ezehmark001@gmail.com",
  ];

  const [mailing, setMailing] = useState(false);

  const [myEmail, setMyEmail] = useState("");
  const [isEmail, setIsEmail] = useState(false);
  const handleIsEmail = (txt) => {
    if (txt.length >= 8 && txt.includes("@gmail.com")) {
      setMyEmail(txt);
      setIsEmail(true);
    } else {
      setIsEmail(false);
    }
  };
  const sendMail = async () => {
    setMailing(true);

    await Promise.all(
      email.map(
        async (user) =>
          await axios
            .post("https://mybackend-oftz.onrender.com/send-mail", {
              recipient: isEmail ? myEmail : user,
              subject: "Web and App Technology Simplified",
              message: isEmail
                ? `Dear ${myEmail.split("@")[0]},\nThe best tech hub is here! At BytanceTech, you get your web/app development done by experienced and professional developers. Reach out to us today for projects like static and dynamic websites, apps and web apps, Search Engine Optimizations(SEO), cybersecurity, productive and uptime robots to fast-track social media engagements, and many more in-demand services\nYou have this rare privilege to discuss with the team-lead:\nMessage Engnr. Mark Ezeh on WhatsApp⤵️\n	 https://wa.me/2349036202766`
                : `Dear ${user.split("@")[0]},\nThe best tech hub is here! At BytanceTech, you get your web/app development done by experienced and professional developers. Reach out to us today for projects like static and dynamic websites, apps and web apps, Search Engine Optimizations(SEO), cybersecurity, productive and uptime robots to fast-track social media engagements, and many more in-demand services\nYou have this rare privilege to discuss with the team-lead:\nMessage Engnr. Mark Ezeh on WhatsApp⤵️\n		 https://wa.me/2349036202766`,
            })

            .then((response) => {setUserDetails(response.data.message);if(response.status === 200){
		    setNotifyMsg(response.data.msg);dropDownChanger()}
		    else{setNotifyMsg("Email failed to send");dropDownChanger()}
	    })
            .catch((error) => setUserDetails(error.response.data.message))
            .finally(() => {
              setMailing(false);
            }),
      ),
    );
  };
  const [focused2, setFocused2] = useState(false);

  const [isfetch, setFetch] = useState(false);
  const [upload, setUpload] = useState(false);

  const [typing, setTyping] = useState("User typing.");
  let typingTime = useRef(null);
  let dotsInterval = useRef(null);
  const typeSetter = () => {
    setBorderCheck(true);
    let dots = ["User typing.", "User typing..", "User typing..."];
    let index = 0;
    if (typingTime.current) {
      clearTimeout(typingTime.current);
    }

    dotsInterval.current = setInterval(() => {
      setTyping(dots[index]);
      index = (index + 1) % dots.length;
    }, 800);

    typingTime.current = setTimeout(() => {
      clearInterval(dotsInterval);
      setBorderCheck(false);
    }, 2500);
  };

  return (
    <LinearGradient
      colors={[
        "#2e4a5f",
        "#2e4a5f",
        "#00d4d4",
        "#00d4d4",
        "#2e4a5f",
        "#2e4a5f",
      ]}
      start={{ x: 0, y: 0 }}
      end={{ x: 1, y: 1 }}
      style={styles.outer}
    >
      <Animated.View
        style={[
          {
            height: 30,
            position: "absolute",
            borderRadius: 10,
            width: 200,
            top: -40,
            backgroundColor: "#feb819",
            opacity: backendActive ? 1 : 0,
            alignItems: "center",
            zIndex: 160,
            justifyContent: "center",
          },
          backendActive && notifyBoxAnim,
        ]}
      >
        {" "}
        <Text style={{ color: "#2e4a5f", position: "absolute", fontSize: 12 }}>
          {notifyMsg}
        </Text>{" "}
      </Animated.View>
      <ScrollView style={{ height: "100%", width: "100%", paddingBottom: 20 }}>
        <View
          style={{
            position: "absolute",
            top: 8,
            left: 20,
            backgroundColor: "#2e4a5f",
            borderRadius: 4,

            flex: 1,
            justifyContent: "space-between",
            padding: 4,
            alignItems: "center",
            flexDirection: "row",
            gap: 4,
          }}
        >
          {" "}
          <View
            style={{
              height: 15,
              width: 15,
              borderRadius: 7.5,
              backgroundColor: connected ? "#00ff00" : "#ccc",
            }}
          />{" "}
          <Text style={{ fontSize: 10, color: connected ? "#00ff00" : "#ccc" }}>
            {connected ? "Active Data" : "Offline"}
          </Text>{" "}
        </View>
        <Text
          style={{
            top: 5,
            borderRadius: 12,
            padding: 4,
            paddingLeft: 10,
            borderColor: "#00ff00",
            borderWidth: 0,
            backgroundColor: "#feb819",
            opacity: borderCheck ? 1 : 0,
            position: "absolute",
            right: "10%",
            color: "#2e4a5f",
            width: 95,
            textAlign: "left",
            fontSize: 12,
          }}
        >
          {typing}
        </Text>

        <View
          style={[
            {
              borderWidth: 0.5,
              borderTopWidth: 1,
              borderColor: "#00d4d4",
              overflow: "hidden",
              borderTopColor: focused || focused2 ? "#00ff00" : "#00d4d4",
            },
            styles.container,
          ]}
        >
          <TouchableOpacity
            disabled={mailing}
            onPress={() => {
              sendMail();
              handleConnection();
            }}
            style={{
              position: "absolute",
              bottom: 86,
              backgroundColor: "black",
              borderRadius: 5,
              padding: 10,
              zIndex: 70,
              width: 100,
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            {" "}
            <Text style={{ fontSize: 14, color: "red" }}>
              {mailing ? "Sending..." : "Send Mail"}
            </Text>
          </TouchableOpacity>
          <Animated.View
            style={[
              {
                backgroundColor: "#00d4d4",
                left: "10%",
                top: 346,
                color: "#2e4a5f",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                position: "absolute",
                padding: 10,
                width: 110,
                fontSize: 14,
                zIndex: 55,
                borderRadius: 10,
              },
              appearAnim1,
            ]}
          >
            {" "}
            <TouchableOpacity
              onPress={handleFetch}
              disabled={loading}
              style={{
                flex: 1,
                alignItems: "center",
                justifyContent: "center",
                flex: 1,
              }}
            >
              <Text style={{ textWrap: "nowrap", color: "black" }}>
                {isfetch ? "Loading..." : "Fetch Now"}
              </Text>
            </TouchableOpacity>{" "}
          </Animated.View>

          {loading ? (
            <View
              style={{
                zIndex: 150,
                height: "1200%",
                width: "120%",
                alignItems: "center",
                backgroundColor: "rgba(46,74,95,0.4)",
                justifyContent: "center",
                position: "absolute",
              }}
              intensity={5}
              tint={"dark"}
            >
              <ActivityIndicator
                style={{
                  padding: 20,
                  backgroundColor: "black",
                  borderRadius: 10,
                  alignSelf: "center",
                  position: "absolute",
                }}
                size={"large"}
                color={"blue"}
              />
            </View>
          ) : (
            <View
              style={[{ opacity: isUserDetails ? 1 : 0 }, styles.userDetails]}
            >
              <Text
                style={{
                  color: "black",
                  margin: 10,
                  opacity: 1,
                }}
              >
                {" "}
                {userDetails}
              </Text>
            </View>
          )}
          <Animated.View style={[styles.uploadBtn, appearAnim]}>
            <TouchableOpacity
              onPress={() => {
                handlePost();
                triggerScroll();
              }}
              disabled={loading}
              style={{
                position: "absolute",
                alignItems: "center",
                justifyContent: "center",
                flex: 1,
              }}
            >
              <Text style={{ color: "#ccc", fontSize: 14, padding: 10 }}>
                {upload ? "Uploading.." : "Upload"}
              </Text>
            </TouchableOpacity>
          </Animated.View>
          {visible && (
            <>
              <LinearGradient
                colors={[
                  "rgba(46,74,95,1)",
                  "rgba(46,74,95,1)",
                  "rgba(46,74,95,0.8)",
                  "transparent",
                  "transparent",
                  "transparent",
                  "transparent",
                  "transparent",
                  "rgba(46,74,95,0.8)",
                  "rgba(46,74,95,1)",
                  "rgba(46,74,95,1)",
                ]}
                start={{ x: 1, y: 0 }}
                end={{ x: 0, y: 1 }}
                style={{
                  bottom: 300,
                  borderRadius: 0,
                  zIndex: 150,
                  position: "absolute",
                  height: 50,
                  width: "100%",
                }}
                pointerEvents={"none"}
              ></LinearGradient>

              <Animated.View
                style={[
                  {
                    bottom: 300,
		    height:50,
                    borderRadius: 20,
                    zIndex: 100,
                    position: "absolute",
                    alignSelf:"flex-start",
		    flexDirection:"row",
		    alignItems:"center",
		    justifyContent:"center",
		    alignItems:"center",
		    
                  },
                  ListAnim,
                ]}
              >
                <FlatList
                  horizontal={true}
                  showsHorizontalScrollIndicator={true}
                  data={myPeople}
		  extraData={selected}
                  keyExtractor={(item) => item.id}
                  renderItem={({ item }) => {
                    const isSelected = selected.includes(item.id);
		    const pressSearch =()=>{
		    setName(item.name);
		    setTimeout(()=>{
		    handleFetch(item.name)},1000)}

                    return (
                      <View
                        style={{
                          height: 40,
                          width:100,
                          shadowOffset: { width: 0, height: 2 },
			  margin:5, 
                          marginBottom:isSelected? 5:0,
                          shadowColor: "rgba(0,0,0,0.3)",
                          elevation: 4,
                          justifyContent: "center",
                          alignItems: "center",
                        }}
                      >
                        <TouchableOpacity
                          onPress={() => {
			    pressSearch(item.name);
                            setSelected((prev) =>
                              prev.includes(item.id)
                                ? prev.filter((id) => id !== item.id)
                                : [...prev, item.id],
                            );
                          }}
                          style={{
                            height: 40,
                            width: 100,
                            borderRadius: isSelected?10:15,

                            justifyContent: "center",
                            backgroundColor: isSelected ? "#feb819" : "#00cdde",
                            alignItems: "center",
                          }}
                        >
                          <Text
                            style={{ color: isSelected ? "black" : "black" }}
                          >
                            {item.name}
                          </Text>
                          <Text
                            style={{
                              color: isSelected ? "#ccc" : "black",
                              fontSize: 10,
                            }}
                          >
                            {`Age: ${item.age}`}
                          </Text>
                        </TouchableOpacity>
                      </View>
                    );
                  }}
                />
              </Animated.View>
            </>
          )}

          <Text
            style={{
              position: "absolute",
              bottom: 130,
              padding: 8,
              borderRadius: 5,
              fontSize: 12,
              width: "80%",
              zIndex: 50,
              borderWidth: 0.5,
              borderColor: "#00d4d4",
              textAlign: "center",
              color: "#00d4d4",
            }}
          >
            This app enables you to fetch saved users from Mark's backend
            database. You can also press "Upload" to send new users to the DB.
            Happy using ✔️
          </Text>

          <Text
            style={{
              position: "absolute",
              bottom: 45,
              padding: 8,
              borderRadius: 5,
              fontSize: 12,
              width: "80%",
              zIndex: 50,
              borderWidth: 0,
              borderColor: "#ecc37e",
              textAlign: "center",
              color: "#ecc37e",
            }}
          >
            Clicking the button "Send Mail" will send email message to specific
            recipients ✔️
          </Text>

          <Text style={styles.feedback}>{feedback}</Text>

          <TextInput
            style={[
              styles.input,
              {
                borderColor: focused ? "#00ff00" : "#ecc37e",
                backgroundColor: focused ? "rgba(215,230,249,0)" : "#2e4a5f",
                borderWidth: focused ? 1.5 : 1,
                color: focused ? "#feb819" : "#ecc37e",
                textAlignVertical: "top",
                outlineWidth: 0,
              },
            ]}
            returnKeyType="Enter"
            onFocus={() => {
              setFocused(true);
              handleConnection();
            }}
            onBlur={() => setFocused(false)}
            placeholder={focused ? "Start typing..." : "Type Name"}
            multiline={true}
            numberOfLines={3}
            value={name}
            onSubmitEditing={() => emailRef.current.focus()}
            onChangeText={(text) => {
              typeSetter();
              setName(text);
            }}
          />
          <TextInput
            style={[
              styles.input2,
              {
                backgroundColor: focused2 ? "transparent" : "#2e4a5f",
                color: focused2 ? "#feb819" : "#ecc37e",
                borderColor: focused2 ? "#00ff00" : "#feb819",
                borderWidrh: focused2 ? 1.5 : 1,
              },
            ]}
            placeholder={focused2 ? "eg. you@gmail.com" : "Enter email"}
            onChangeText={(txt) => {
              typeSetter();
              handleIsEmail(txt);
            }}
            onFocus={() => setFocused2(true)}
            onBlur={() => setFocused2(false)}
            multiline={true}
            returnKeyType="Done"
            ref={emailRef}
          />
        </View>
      </ScrollView>
    </LinearGradient>
  );
};

const styles = StyleSheet.create({
  input: {
    position: "absolute",
    top: 35,
    width: "90%",
    height: 40,
    borderRadius: 20,
    padding: 10,
    backgroundColor: "#cfccc5",
    borderWidth: 1,
    borderColor: "black",
  },

  input2: {
    position: "absolute",
    top: 70,
    width: "90%",
    height: 40,
    borderRadius: 20,
    padding: 10,
    marginTop: 15,
    backgroundColor: "#2e4a5f",
    borderWidth: 1,
    outlineWidth: 0,
    borderColor: "#ecc37e",
  },
  outer: {
    flex: 1,
    backgroundColor: "#ecc37e",
    justifyContent: "center",
    alignItems: "center",
  },
  container: {
    height: 600,
    width: "95%",
    backgroundColor: "#2e4a5f",
    textWrap: "noWrap",
    alignSelf: "center",
    justifyContent: "center",
    alignItems: "center",
    position: "absolute",
    borderRadius: 20,
    top: 40,
    elevation: 0,
    shadowRadius: 5,

    shadowOffset: { width: 0, height: 4 },
    shadowColor: "rgba(0,0,0,0.2)",
  },
  userDetails: {
    top: 150,
    position: "absolute",
    width: "85%",
    borderRadius: 15,
    borderWidth: 0.5,
    borderColor: "black",
    textAlign: "center",
    color: "green",
    alignItems: "center",

    justifyContent: "center",
    backgroundColor: "#00ff00",
  },
  feedback: {
    top: 5,
    borderRadius: 15,
    padding: 10,
    backgroundColor: "#2e4a5f",
    color: "red",
    position: "absolute",
  },
  uploadBtn: {
    backgroundColor: "#ea0707",
    right: "10%",
    position: "absolute",
    alignSelf: "center",
    top: 346,
    fontWeight: "bold",
    width: 110,
    height: 40,
    zIndex: 60,

    padding: 10,
    borderRadius: 10,
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
  },
});

export default MyApp;
