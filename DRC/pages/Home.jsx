import React from "react";
import { useNavigate } from "react-router-dom";
import {
  MapPin,
  Navigation,
  CloudSun,
  Wind,
  Trophy,
  Briefcase,
  FlaskConical,
  History,
} from "lucide-react";

export default function Home() {
  const navigate = useNavigate();

  const tools = [
    { title: "MY BAG", icon: Briefcase, path: "/bag" },
    { title: "COURSES", icon: MapPin, path: "/courses" },
    { title: "GOLF LAB", icon: FlaskConical, path: "/lab" },
    { title: "SCORECARD", icon: Trophy, path: "/scorecard" },
    { title: "ROUND HISTORY", icon: History, path: "/history" },
  ];

  return (
    <main className="min-h-screen bg-[#06110d] text-[#f5e6ad]">
      <section
        className="relative min-h-screen overflow-hidden px-4 pb-28 pt-6"
        style={{
          background:
            "radial-gradient(circle at 50% 0%, #183b2a 0%, #091a13 40%, #020705 100%)",
        }}
      >
        <div className="mx-auto max-w-md">
          <header className="text-center">
            <div className="text-xs tracking-[0.42em] text-[#c9a84f]">
              DRC GOLF
            </div>

            <h1
              className="mt-2 text-3xl font-bold tracking-[0.12em]"
              style={{
                color: "#d9bd68",
                textShadow:
                  "0 1px 0 #fff2ae, 0 3px 6px #000, 0 0 18px rgba(217,189,104,.25)",
              }}
            >
              TOURBILLION
            </h1>

            <div className="mx-auto mt-3 h-px w-40 bg-gradient-to-r from-transparent via-[#d9bd68] to-transparent" />

            <p className="mt-3 text-sm text-[#e8dcae]">
              Welcome Dale
            </p>
          </header>

          <section className="mt-5 overflow-hidden rounded-[24px] border border-[#a98b3e] bg-[#081711] shadow-2xl">
            <div
              className="relative h-52 bg-cover bg-center"
              style={{
                backgroundImage:
                  "linear-gradient(to top, rgba(2,7,5,.95), rgba(2,7,5,.12)), url('/opening_luxury_bg.png')",
              }}
            >
              <div className="absolute inset-x-0 bottom-0 p-5">
                <div className="text-xs tracking-[0.2em] text-[#d9bd68]">
                  QUICK START
                </div>

                <h2 className="mt-1 text-xl font-semibold text-white">
                  Mercure Capricorn Resort Golf
                </h2>

                <div className="mt-2 flex items-center gap-2 text-sm text-[#d8d8cf]">
                  <MapPin size={15} />
                  <span>Capricorn Coast, Queensland</span>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-3 border-t border-[#6d5a2a] bg-[#07120e]">
              <div className="p-3 text-center">
                <Navigation
                  size={18}
                  className="mx-auto mb-1 text-[#d9bd68]"
                />
                <div className="text-[10px] text-[#9c9c92]">GPS</div>
                <div className="text-xs text-white">READY</div>
              </div>

              <div className="border-x border-[#3d351f] p-3 text-center">
                <CloudSun
                  size={18}
                  className="mx-auto mb-1 text-[#d9bd68]"
                />
                <div className="text-[10px] text-[#9c9c92]">WEATHER</div>
                <div className="text-xs text-white">LIVE</div>
              </div>

              <div className="p-3 text-center">
                <Wind
                  size={18}
                  className="mx-auto mb-1 text-[#d9bd68]"
                />
                <div className="text-[10px] text-[#9c9c92]">WIND</div>
                <div className="text-xs text-white">LIVE</div>
              </div>
            </div>
          </section>

          <button
            type="button"
            onClick={() => navigate("/caddie")}
            className="mt-5 w-full rounded-2xl border border-[#e0bd58] bg-gradient-to-b from-[#a51d19] to-[#630d0b] px-5 py-4 font-bold tracking-[0.14em] text-[#ffe59a] shadow-xl"
          >
            START CADDIE ENGINE
          </button>

          <section className="mt-5 grid grid-cols-2 gap-3">
            {tools.map(({ title, icon: Icon, path }) => (
              <button
                key={title}
                type="button"
                onClick={() => navigate(path)}
                className="rounded-2xl border border-[#8f7637] bg-gradient-to-b from-[#10271c] to-[#07110d] p-4 text-left shadow-lg"
              >
                <Icon size={24} className="mb-3 text-[#d9bd68]" />
                <div className="text-xs font-semibold tracking-[0.08em] text-[#f1dfa4]">
                  {title}
                </div>
              </button>
            ))}
          </section>
        </div>
      </section>
    </main>
  );
}
