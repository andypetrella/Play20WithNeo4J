
$ ->
    $("#create_user").bind "click", (event) =>
      $.post(
        playRoutes.controllers.Users.create().url
        {
          "firstname" : $("#create_user_firstName").val()
        }
        (ok) ->
      )

    $("#knows").bind "click", (event) =>
      $.post(
        playRoutes.controllers.Users.userKnows($("#knows_start").val()).url
        {
          "knows[0]" : $("#knows_end").val()
        }
        (ok) ->
      )

    $("#create_group").bind "click", (event) =>
      $.post(
        playRoutes.controllers.Groups.create().url
        {
          "name" : $("#create_group_name").val()
        }
        (ok) ->
      )

    $("#grouping").bind "click", (event) =>
      $.post(
        playRoutes.controllers.Groups.addUsers($("#grouping_group").val()).url
        {
          "user[0]" : $("#grouping_user").val()
        }
        (ok) ->
      )

    Renderer = (canvas) ->
      canvas = $(canvas).get(0)
      ctx = canvas.getContext("2d");
      particleSystem = undefined
      that = {
        init: (system) =>
          particleSystem = system

          particleSystem.screenSize(canvas.width, canvas.height)
          particleSystem.screenPadding(80)

          that.initMouseHandling()

        redraw: () =>
          ctx.fillStyle = "white"
          ctx.fillRect(0,0, canvas.width, canvas.height)

          particleSystem.eachEdge((edge, pt1, pt2) =>
            if (edge.data && edge.data.type && edge.data.type is "knows")
              ctx.strokeStyle = "rgba(255,0,0, .666)"
            else
              ctx.strokeStyle = "rgba(0,0,0, .333)"

            ctx.lineWidth = 1
            ctx.beginPath()
            ctx.moveTo(pt1.x, pt1.y)
            ctx.lineTo(pt2.x, pt2.y)
            ctx.stroke()
          )

          particleSystem.eachNode((node, pt) =>
            w = 20
            if (node.data.root)
              ctx.beginPath()
              ctx.arc(pt.x, pt.y, w/2, 0, 2 * Math.PI, false)
              ctx.fillStyle = "#8ED6FF"
              ctx.lineWidth = 5
              ctx.strokeStyle = "black";
              ctx.fill()
              ctx.stroke()
            else
              #draw body
              ctx.beginPath()
              ctx.moveTo(pt.x, pt.y-w/2)
              ctx.lineTo(pt.x+w/2, pt.y+w/2)
              ctx.lineTo(pt.x-w/2, pt.y+w/2)
              ctx.lineTo(pt.x, pt.y-w/2)
              ctx.fillStyle = "blue"
              if (node.data.enabledGroup)
                ctx.lineWidth = 5
                ctx.strokeStyle = "rgba(255,0,0, .333)";
              else
                ctx.lineWidth = 1
                ctx.strokeStyle = "black";
              ctx.fill()
              ctx.stroke()

              #draw head
              ctx.beginPath()
              ctx.arc(pt.x, pt.y-w/2, w/3, 2 * Math.PI, false)

              ctx.fillStyle = "blue"
              if (node.data.enabledGroup)
                ctx.lineWidth = 3
                ctx.strokeStyle = "rgba(255,0,0, .333)";
              else
                ctx.lineWidth = 1
                ctx.strokeStyle = "black";
              ctx.fill()
              ctx.stroke()

              #draw first name adn node id
              ctx.fillText(node.data.id + " - " + node.data.firstName, pt.x, pt.y+w);
          )

        initMouseHandling: () =>
          #on mousedown => select the nearest node and show all edges starting from it
          $(canvas).bind "click", (e) =>
            pos = $(canvas).offset();
            _mouseP = arbor.Point(e.pageX-pos.left, e.pageY-pos.top)
            node = particleSystem.nearest(_mouseP)
            node = node.node
            #first remove all other knows links
            particleSystem.eachEdge((edge, pt1, pt2) =>
                if (edge.data.type && edge.data.type is "knows")
                  particleSystem.pruneEdge(edge)
            )

            $.get(
                playRoutes.controllers.Users.j_knows(node.data.id).url
                (knows) =>
                  particleSystem.eachNode((n, pt) =>
                    particleSystem.addEdge(node, n, {type:"knows"}) for k in knows when (n.data.id is k.id)
                  )
            )

      }


    users = {}

    start = () =>
      #create the Particle System
      sys = arbor.ParticleSystem(1000, 600, 0.5) #// create the system with sensible repulsion/stiffness/friction
      sys.parameters({gravity:true}) #// use center-gravity to make the graph settle nicely (ymmv)
      sys.renderer = Renderer("#viewport") #// our newly created renderer will have its .init() method called shortly by sys...

      #create a virtual root node
      root = sys.addNode("root", {root:true})

      for k,u of users
        #add all users as Node to the graph
        n = sys.addNode("user-"+k, u)
        #to which all users will be connected
        sys.addEdge(root, n)



      #get all groups and render them in the select
      $.get(
        playRoutes.controllers.Groups.j_all().url
        (gs) ->
          $("#groups").append($("<option value='"+ g.id + "'>" + g.id + "-" + g.name + "</option>")) for g in gs
      )

      #when changing group, highlight the users participating in
      $("#groups").bind "change", (event) =>
        groupId = parseInt($("#groups option:selected").val())

        if (groupId isnt -1)
          $.get(
            playRoutes.controllers.Groups.j_users(groupId).url
            (us) ->
              sys.eachNode((n, pt) =>
                ok = false
                for u in us
                  if (u.id is n.data.id)
                    ok = true
                    break
                n.data.enabledGroup = ok
              )
              sys.renderer.redraw()
          )
        else
          sys.eachNode((n, pt) =>
            n.data.enabledGroup = false
          )
          sys.renderer.redraw()


    #Get all users and add them to the graph
    routeToAllUsers = playRoutes.controllers.Users.j_all().url
    $.get(
      routeToAllUsers
      (us) ->
        users[u.id] = u for u in us
        start()
    )

